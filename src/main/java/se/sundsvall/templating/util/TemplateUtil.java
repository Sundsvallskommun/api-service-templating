package se.sundsvall.templating.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import se.sundsvall.templating.domain.TemplateType;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class TemplateUtil {

	private TemplateUtil() {}

	public static TemplateType getTemplateType(final String templateContent) {
		return getTemplateType(templateContent.getBytes(UTF_8));
	}

	public static TemplateType getTemplateType(final byte[] templateContent) {
		try (var in = new ByteArrayInputStream(templateContent); var _ = OPCPackage.open(in)) {
			return TemplateType.WORD;
		} catch (IOException | InvalidFormatException | NotOfficeXmlFileException _) {
			return TemplateType.PEBBLE;
		}
	}

	public static String bytesToString(byte[] data) {
		return new String(data, UTF_8);
	}

	public static String encodeBase64(final String data) {
		return Base64.getEncoder().encodeToString(data.getBytes(UTF_8));
	}

	public static String encodeBase64(final byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	public static byte[] decodeBase64(final String data) {
		return Base64.getDecoder().decode(data);
	}
}
