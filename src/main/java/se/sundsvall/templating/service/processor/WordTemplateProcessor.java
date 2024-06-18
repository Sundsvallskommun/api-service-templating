package se.sundsvall.templating.service.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.PositionInParagraph;
import org.apache.poi.xwpf.usermodel.TextSegment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTProofErr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.stereotype.Component;

import se.sundsvall.templating.exception.TemplateException;

@Component
public class WordTemplateProcessor implements TemplateProcessor<byte[]> {

    private static final String[] PLACEHOLDER_FORMATS = {"{{%s}}", "{{ %s }}", "{{ %s}}", "{{%s }}"};

    private static final String CONTENT_TYPE_TEMPLATE = "application/vnd.openxmlformats-officedocument.wordprocessingml.template.main+xml";
    private static final String CONTENT_TYPE_MACRO_ENABLED_TEMPLATE = "application/vnd.ms-word.template.macroEnabledTemplate.main+xml";
    private static final String CONTENT_TYPE_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml";

    @Override
    public byte[] process(final byte[] template, final Map<String, Object> parameters) {
        var flattenedParameters = flattenMap(parameters);

        try (var in = new ByteArrayInputStream(template);
             var document = new XWPFDocument(in);
             var out = new ByteArrayOutputStream()) {

            // Process document header(s), if any
            for (var header : document.getHeaderList()) {
                // Process document header body, if any
                replaceInParagraphs(header.getParagraphs(), flattenedParameters);
                // Process document header table cells, if any
                replaceInTables(header.getTables(), flattenedParameters);
            }

            // Process the document body
            replaceInParagraphs(document.getParagraphs(), flattenedParameters);
            // Process the document body table cells, if any
            replaceInTables(document.getTables(), flattenedParameters);
            // Process content controls
            replaceInContentControls(document, flattenedParameters);

            // Process document footer(s), if any
            for (var footer : document.getFooterList()) {
                // Process document footer body, if any
                replaceInParagraphs(footer.getParagraphs(), flattenedParameters);
                // Process document footer table cells, if any
                replaceInTables(footer.getTables(), flattenedParameters);
            }

            // Write the updated document
            document.write(out);

            var result = out.toByteArray();
            try (var opcPackageIn = new ByteArrayInputStream(result);
                 var opcPackage = OPCPackage.open(opcPackageIn)) {

                // If the input was a Word template (.dotx/.dotm), change the output content type to a regular document
                if (opcPackage.getParts().stream().map(PackagePart::getContentType).anyMatch(this::isTemplate)) {
                    opcPackage.replaceContentType(CONTENT_TYPE_TEMPLATE, CONTENT_TYPE_DOCUMENT);
                    opcPackage.replaceContentType(CONTENT_TYPE_MACRO_ENABLED_TEMPLATE, CONTENT_TYPE_DOCUMENT);

                    try (var opcPackageOut = new ByteArrayOutputStream()) {
                        opcPackage.save(opcPackageOut);
                        result = opcPackageOut.toByteArray();
                    }
                }

                return result;
            }
        } catch (IOException | InvalidFormatException e) {
            throw new TemplateException("Unable to process Word template", e);
        }
    }

    private boolean isTemplate(final String contentType) {
        return CONTENT_TYPE_TEMPLATE.equals(contentType) || CONTENT_TYPE_MACRO_ENABLED_TEMPLATE.equals(contentType);
    }

    private static void replaceInParagraphs(final List<XWPFParagraph> paragraphs, final Map<String, Object> parameters) {
        for (var paragraph : paragraphs) {
            for (var replacement : parameters.entrySet()) {
                // Naively "guess" the placeholder formatting with regard to spacing
                for (var placeholderFormat : PLACEHOLDER_FORMATS) {
                    replaceTextSegment(paragraph, placeholderFormat.formatted(replacement.getKey()), replacement.getValue());
                }
            }
        }
    }

    private static void replaceInTables(final List<XWPFTable> tables, final Map<String, Object> parameters) {
        for (var table : tables) {
            for (var row : table.getRows()) {
                for (var cell : row.getTableCells()) {
                    replaceInParagraphs(cell.getParagraphs(), parameters);
                }
            }
        }
    }


    private static void replaceTextSegment(final XWPFParagraph paragraph, final String textToFind, final Object replacement) {
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

    private static TextSegment searchText(final XWPFParagraph paragraph, final String textToFind) {
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

    private static void replaceInContentControls(final XWPFDocument document, final Map<String, Object> parameters) {
        try (var xmlCursor = document.getDocument().getBody().newCursor()) {
            var qNameSdt = new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "sdt", "w");

            while (xmlCursor.hasNextToken()) {
                var tokenType = xmlCursor.toNextToken();

                if (tokenType.isStart() && qNameSdt.equals(xmlCursor.getName()) && xmlCursor.getObject() instanceof CTSdtBlock ctSdtBlock) {
                    var content = ctSdtBlock.getSdtContent();

                    for (var ctp : content.getPArray()) {
                        for (var ctr : ctp.getRArray()) {
                            for (var cttext : ctr.getTArray()) {
                                var stringValue = cttext.getStringValue();
                                for (var parameter : parameters.entrySet()) {
                                    // Naively "guess" the placeholder formatting with regard to spacing
                                    for (var placeholderFormat : PLACEHOLDER_FORMATS) {
                                        stringValue = stringValue.replace(placeholderFormat.formatted(parameter.getKey()), parameter.getValue().toString());
                                    }
                                }
                                cttext.setStringValue(stringValue);
                            }
                        }
                    }
                }
            }
        }
    }


    Map<String, Object> flattenMap(final Map<String, Object> map) {
        return map.entrySet().stream()
            .flatMap(this::flatten)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Stream<Map.Entry<String, Object>> flatten(final Map.Entry<String, Object> entry) {
        if (entry.getValue() instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            var nested = (Map<String, Object>) entry.getValue();

            return nested.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(entry.getKey() + "." + e.getKey(), e.getValue()))
                .flatMap(this::flatten);
        }
        return Stream.of(entry);
    }
}
