package se.sundsvall.templating.service.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.SectPr;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static se.sundsvall.templating.service.processor.HtmlValidator.validateHtml;

@Component
public class WordTemplateProcessor implements TemplateProcessor<byte[]> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordTemplateProcessor.class);

	@Override
	public byte[] process(final byte[] template, final Map<String, Object> parameters) {
		try {
			final var inputStream = new ByteArrayInputStream(template);
			final var wordMLPackage = WordprocessingMLPackage.load(inputStream);
			final var mainDocumentPart = wordMLPackage.getMainDocumentPart();

			for (final var entry : parameters.entrySet()) {
				if (entry.getValue() instanceof String stringValue)
					replacePlaceholderWithHtml(wordMLPackage, mainDocumentPart, entry.getKey(), stringValue);
			}

			final var outputStream = new ByteArrayOutputStream();
			Docx4J.save(wordMLPackage, outputStream);
			return outputStream.toByteArray();
		} catch (final ThrowableProblem e) {
			throw e;
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to process Word template");
		}
	}

	/**
	 * Create a pattern for the given key that matches the correct placeholder syntax. {{ key }}, {{key}}, {{key }}. {{
	 * key}}
	 *
	 * @param  key The key parameter key
	 * @return     pattern for the given key
	 */
	private Pattern placeholderPattern(final String key) {
		return Pattern.compile("\\{\\{\\s*" + Pattern.quote(key) + "\\s*}}");
	}

	private void replacePlaceholderWithHtml(final WordprocessingMLPackage wordMLPackage, final MainDocumentPart mainDocumentPart, final String key, final String value) throws Docx4JException {
		final var paragraphs = getAllParagraphs(mainDocumentPart);

		final var pattern = placeholderPattern(key);

		for (final var paragraph : paragraphs) {
			final var text = getText(paragraph);

			if (pattern.matcher(text).find()) {

				final var savedSectPr = extractSectPr(paragraph);

				validateHtml(key, value);
				final var sanitizedValue = sanitizeHtml(value);
				final var wrappedHtml = wrapHtml(sanitizedValue);
				final var xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);
				final var wordMLContent = xhtmlImporter.convert(wrappedHtml, null);

				final var parent = (ContentAccessor) paragraph.getParent();
				final var parentContent = parent.getContent();
				final var index = parentContent.indexOf(paragraph);

				parentContent.remove(index);
				parentContent.addAll(index, wordMLContent);

				if (savedSectPr != null) {
					applySectPrToLastParagraph(wordMLContent, savedSectPr);
				}

				break;
			}
		}
	}

	private SectPr extractSectPr(final P p) {
		if (p.getPPr() == null) {
			return null;
		}
		return p.getPPr().getSectPr();
	}

	private void applySectPrToLastParagraph(final List<Object> newContent, final SectPr sectPr) {
		for (int i = newContent.size() - 1; i >= 0; i--) {
			final var object = newContent.get(i);

			if (object instanceof P p) {
				if (p.getPPr() == null) {
					p.setPPr(new PPr());
				}
				p.getPPr().setSectPr(sectPr);
				return;
			}
		}
	}

	String sanitizeHtml(final String html) {
		final var document = Jsoup.parseBodyFragment(html);
		document.outputSettings()
			.syntax(Document.OutputSettings.Syntax.xml)
			.escapeMode(Entities.EscapeMode.xhtml)
			.charset("UTF-8")
			.prettyPrint(false);
		return document.body().html();
	}

	/**
	 * Wrap given HTML to ensure it is a proper HTML document and have a default styling
	 *
	 * @param  html given HTML string
	 * @return      a formatted HTML document as a String
	 */
	String wrapHtml(final String html) {
		return """
			<!DOCTYPE html>
			<html>
			<head>
			    <meta charset="UTF-8"/>
			    <style>
			        body { margin: 0; padding: 0; }
			        table { page-break-inside: avoid; }
			    </style>
			</head>
			<body>
			    %s
			</body>
			</html>
			""".formatted(html);
	}

	/**
	 * Get the text from the given paragraph
	 *
	 * @param  paragraph given paragraph
	 * @return           the text
	 */
	private String getText(final P paragraph) {
		return TextUtils.getText(paragraph);
	}

	/**
	 * Helper to retrieve all Paragraph elements from the given MainDocumentPart
	 *
	 * @param  mainDocumentPart given MainDocumentPart
	 * @return                  List with all Paragraph elements.
	 */
	private List<P> getAllParagraphs(final MainDocumentPart mainDocumentPart) {
		return getAllElementsFromObject(mainDocumentPart, P.class);
	}

	/**
	 * Helper to retrieve a given element from the given ContentAccessor
	 *
	 * @param  obj      given ContentAccessor
	 * @param  toSearch element to look for
	 * @param  <T>      Given class
	 * @return          List with all requested elements from the given ContentAccessor
	 */
	private <T> List<T> getAllElementsFromObject(final Object obj, final Class<T> toSearch) {
		final var result = new ArrayList<T>();

		if (obj instanceof ContentAccessor contentAccessor) {
			for (final var child : contentAccessor.getContent()) {
				if (toSearch.isInstance(child)) {
					result.add(toSearch.cast(child));
				}
				result.addAll(getAllElementsFromObject(child, toSearch));
			}
		}

		return result;
	}
}
