package se.sundsvall.templating.service.processor;

import jakarta.xml.bind.JAXBElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.springframework.stereotype.Component;
import se.sundsvall.templating.exception.TemplateException;
import se.sundsvall.templating.service.webble.WebbleContext;
import se.sundsvall.templating.service.webble.WebbleEngine;

@Component
public class WebbleTemplateProcessor implements TemplateProcessor<byte[]> {

	private static final WebbleContext CONTEXT = new WebbleContext();

	@Override
	public byte[] process(final byte[] decodedContent, final Map<String, Object> parameters) {
		try {
			Path tempFile = Files.createTempFile("tempFile", ".docx");
			Files.write(tempFile, decodedContent);

			var wordMLPackage = WordprocessingMLPackage.load(tempFile.toFile());

			var htmlEntries = parameters.entrySet().stream()
				.peek(entry -> System.out.println("Key: " + entry.getKey()))
				.peek(entry -> System.out.println("Value: " + entry.getValue()))
				.filter(entry -> entry.getValue() instanceof String)
				.filter(entry -> entry.getValue().toString().startsWith("HTML:"))
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), ((String) entry.getValue()).substring("HTML:".length())))
				.toList();

			for (var htmlEntry : htmlEntries) {
				System.out.println("HTML key " + htmlEntry.getKey());
				System.out.println("HTML value " + htmlEntry.getValue());
				var docxObjects = convertHtmlToDocxObjects(htmlEntry.getValue(), wordMLPackage);
				replacePlaceholderWithObjects(wordMLPackage.getMainDocumentPart(), htmlEntry.getKey(), docxObjects);
			}

			wordMLPackage.save(tempFile.toFile());

			var textEntries = parameters.entrySet().stream()
				.filter(entry -> entry.getValue() instanceof String)
				.filter(entry -> !((String) entry.getValue()).startsWith("HTML:"))
				.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()))
				.toList();

			for (var textEntry : textEntries) {
				CONTEXT.bind(textEntry.getKey(), textEntry.getValue());
			}
			return Files.readAllBytes(WebbleEngine.evaluate(tempFile, CONTEXT));
		} catch (IOException e) {
			throw new TemplateException(e);
		} catch (Docx4JException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Konverterar HTML till en lista av docx4j-objekt (t.ex. stycken, tabeller).
	 */
	private static List<Object> convertHtmlToDocxObjects(String html, WordprocessingMLPackage wordMLPackage) throws Exception {
		XHTMLImporterImpl xhtmlImporter = new XHTMLImporterImpl(wordMLPackage);
		return xhtmlImporter.convert(html, null);
	}

	/**
	 * Ersätt en placeholder med importerade OOXML-objekt (t.ex. genererade tabeller).
	 */
	private static void replacePlaceholderWithObjects(MainDocumentPart documentPart, String placeholder, List<Object> newObjects) {
		// 1) Hitta alla text-noder som innehåller placeholdern
		List<Text> allTextNodes = getAllElementFromObject(documentPart.getJaxbElement(), Text.class);

		for (Text textNode : allTextNodes) {
			String textVal = textNode.getValue();
			if (textVal != null && textVal.contains(placeholder)) {

				// Ta bort själva placeholdern ur texten
				textVal = textVal.replace(placeholder, "");
				textNode.setValue(textVal);

				// Infoga docx4j-objekten direkt efter <w:r> som håller textnoden
				R run = (R) textNode.getParent();
				if (run != null) {
					P parentP = (P) run.getParent();
					if (parentP != null) {
						List<Object> parentContent = parentP.getContent();
						int runIndex = parentContent.indexOf(run);

						// "Packa upp" (unmarshall) objekten så att de kan läggas direkt i parentens content-lista
						List<Object> unwrapped = newObjects.stream()
							.map(XmlUtils::unwrap)
							.collect(Collectors.toList());

						// Infoga
						parentContent.addAll(runIndex + 1, unwrapped);
					}
				}
			}
		}
	}

	/**
	 * Hjälpmetod för att rekursivt hämta alla element av en viss typ (Text, R, P etc.) i OOXML-trädet.
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> getAllElementFromObject(Object obj, Class<T> toSearch) {
		List<T> result = new ArrayList<>();

		if (obj == null) {
			return result;
		}

		// "Packa upp" om vi har en JAXBElement
		if (obj instanceof JAXBElement<?>) {
			obj = ((JAXBElement<?>) obj).getValue();
		}

		// Kolla om objektet är av önskad typ
		if (toSearch.isAssignableFrom(obj.getClass())) {
			result.add((T) obj);
		}
		// Om objektet har "innehåll" (t.ex. P, Tbl, MainDocumentPart),
		// loopa igenom dess barn och rekursivt sök vidare
		else if (obj instanceof ContentAccessor) {
			List<?> children = ((ContentAccessor) obj).getContent();
			for (Object child : children) {
				result.addAll(getAllElementFromObject(child, toSearch));
			}
		}
		return result;
	}

}
