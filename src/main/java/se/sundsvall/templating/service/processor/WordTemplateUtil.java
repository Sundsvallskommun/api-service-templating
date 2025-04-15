package se.sundsvall.templating.service.processor;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.TextSegment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.xmlbeans.XmlCursor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAltChunk;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTProofErr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

final class WordTemplateUtil {

	private WordTemplateUtil() {}

	static boolean replaceBodyElement(final XWPFDocument document, final String textToFind, final String id, final String replacement) throws InvalidFormatException {
		var pos = 0;

		for (var bodyElement : document.getBodyElements()) {
			if (bodyElement instanceof XWPFParagraph paragraph) {
				var text = paragraph.getText();
				if (text != null && text.contains(textToFind)) {
					// Create an XmlCursor at this paragraph
					try (var xmlCursor = paragraph.getCTP().newCursor()) {
						var textValue = xmlCursor.getTextValue();
						// Extract text before and after the placeholder
						var before = textValue.substring(0, textValue.indexOf(textToFind));
						var after = textValue.substring(textValue.indexOf(textToFind) + textToFind.length());

						// Create the HTML document part
						var htmlDocumentPart = createHtmlDocumentPart(document, id, before + replacement + after);
						// Move to the end of the paragraph
						xmlCursor.toEndToken();
						// There must always be a next start token. Either a p or at least sectPr
						var currentToken = xmlCursor.currentTokenType();
						while (currentToken != XmlCursor.TokenType.START) {
							currentToken = xmlCursor.toNextToken();
						}
						// Now we can insert the CTAltChunk here
						var uri = CTAltChunk.type.getName().getNamespaceURI();
						xmlCursor.beginElement("altChunk", uri);
						xmlCursor.toParent();
						var cTAltChunk = (CTAltChunk) xmlCursor.getObject();
						// Set the altChunk's id to reference the given XWPFHtmlDocument
						cTAltChunk.setId(htmlDocumentPart.getId());
						// Now remove the matched IBodyElement
						document.removeBodyElement(pos);

						return true;
					}
				}
			}
			pos++;
		}

		return false;

	}

	private static XWPFHtmlDocumentPart createHtmlDocumentPart(final XWPFDocument document, final String id, final String html) throws InvalidFormatException {
		var oPCPackage = document.getPackage();
		var packagePartName = PackagingURIHelper.createPartName("/word/%s.html".formatted(id));
		var packagePart = oPCPackage.createPart(packagePartName, TEXT_HTML_VALUE);
		var xwpfHtmlDocument = new XWPFHtmlDocumentPart(packagePart, id, html);
		document.addRelation(id, new XWPFHtmlRelation(), xwpfHtmlDocument);
		return xwpfHtmlDocument;
	}

	static final class XWPFHtmlDocumentPart extends POIXMLDocumentPart {

		private final String id;
		private String content;

		XWPFHtmlDocumentPart(final PackagePart part, final String id, final String content) {
			super(part);

			this.id = id;
			this.content = wrapContentIfRequired(content);
		}

		String getId() {
			return id;
		}

		String getContent() {
			return content;
		}

		void setContent(final String content) {
			this.content = wrapContentIfRequired(content);
		}

		@Override
		protected void commit() throws IOException {
			var packagePart = getPackagePart();

			try (var out = packagePart.getOutputStream(); var writer = new OutputStreamWriter(out, UTF_8)) {
				writer.write(content);
			}
		}

		String wrapContentIfRequired(final String content) {
			var result = content;
			if (!content.toLowerCase().startsWith("<body>")) {
				result = "<body>" + content;
			}
			if (!content.toLowerCase().endsWith("</body>")) {
				result += "</body>";
			}
			return result;
		}

	}

	static final class XWPFHtmlRelation extends POIXMLRelation {

		XWPFHtmlRelation() {
			super(TEXT_HTML_VALUE, "http://schemas.openxmlformats.org/officeDocument/2006/relationships/aFChunk", "/word/htmlDoc#.html");
		}
	}

	static void replaceTextSegment(final XWPFParagraph paragraph, final String textToFind, final Object replacement) {
		TextSegment foundTextSegment;

		while ((foundTextSegment = searchText(paragraph, textToFind)) != null) {
			// There may be text before textToFind in the begin run
			var beginRun = paragraph.getRuns().get(foundTextSegment.getBeginRun());
			var beginRunText = beginRun.getText(foundTextSegment.getBeginText());
			// We only need the text before
			var textBeforeBeginRunText = beginRunText.substring(0, foundTextSegment.getBeginChar());
			// There may be text after textToFind in end the run
			var endRun = paragraph.getRuns().get(foundTextSegment.getEndRun());
			var endRunText = endRun.getText(foundTextSegment.getEndText());
			// We only need the text after
			var textAfterEndRunText = endRunText.substring(foundTextSegment.getEndChar() + 1);

			if (foundTextSegment.getEndRun() == foundTextSegment.getBeginRun()) {
				// If we have a single run, we need the text before, the replacement, and the text after
				beginRunText = textBeforeBeginRunText + replacement + textAfterEndRunText;
			} else {
				// If not, we need the text before followed by the replacement in the begin run...
				beginRunText = textBeforeBeginRunText + replacement;
				// ...and the text after in the end run
				endRun.setText(textAfterEndRunText, foundTextSegment.getEndText());
			}

			beginRun.setText(beginRunText, foundTextSegment.getBeginText());

			// Remove runs between begin and end runs
			for (var runBetween = foundTextSegment.getEndRun() - 1; runBetween > foundTextSegment.getBeginRun(); runBetween--) {
				paragraph.removeRun(runBetween);
			}
		}
	}

	static TextSegment searchText(final XWPFParagraph paragraph, final String textToFind) {
		var startingPosition = new PositionInParagraph(0, 0, 0);
		var startRun = startingPosition.getRun();
		var startText = startingPosition.getText();
		var startChar = startingPosition.getChar();
		var beginRunPos = 0;
		var candidateCharPos = 0;
		var beginTextPos = 0;
		var beginCharPos = 0;
		var newList = false;

		var runs = paragraph.getRuns();
		for (var runPos = startRun; runPos < runs.size(); runPos++) {
			var textPos = 0;
			int charPos;
			var ctRun = runs.get(runPos).getCTR();
			try (var c = ctRun.newCursor()) {
				c.selectPath("./*");
				while (c.toNextSelection()) {
					var o = c.getObject();
					switch (o) {
						case CTText ignored -> {
							if (textPos >= startText) {
								var candidate = ((CTText) o).getStringValue();
								if (runPos == startRun) {
									charPos = startChar;
								} else {
									charPos = 0;
								}

								for (; charPos < candidate.length(); charPos++) {
									if ((candidate.charAt(charPos) == textToFind.charAt(0)) && (candidateCharPos == 0)) {
										beginTextPos = textPos;
										beginCharPos = charPos;
										beginRunPos = runPos;
										newList = true;
									}

									if (candidate.charAt(charPos) == textToFind.charAt(candidateCharPos)) {
										if (candidateCharPos + 1 < textToFind.length()) {
											candidateCharPos++;
										} else if (newList) {
											var segment = new TextSegment();
											segment.setBeginRun(beginRunPos);
											segment.setBeginText(beginTextPos);
											segment.setBeginChar(beginCharPos);
											segment.setEndRun(runPos);
											segment.setEndText(textPos);
											segment.setEndChar(charPos);
											return segment;
										}
									} else {
										candidateCharPos = 0;
									}
								}
							}
							textPos++;
						}
						case CTProofErr ignored -> c.removeXml();
						case CTRPr ignored -> {
							// Do nothing
						}
						case null, default -> candidateCharPos = 0;
					}
				}
			}
		}
		return null;
	}
}
