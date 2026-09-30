package se.sundsvall.templating.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Component;
import se.sundsvall.templating.exception.TemplateException;

import static org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND;
import static se.sundsvall.templating.service.RenderingService.fontSupplier;

@Component
public class PdfWatermarker {

	static final String TITLE = "FÖRHANDSGRANSKNING";
	static final String SUBTITLE = "Ej giltig handling";

	private static final String FONT = "/fonts/LiberationSans-Bold.ttf";
	private static final float OPACITY = 0.45f;
	private static final float GRAY = 0.5f;
	private static final float TITLE_WIDTH_OF_DIAGONAL = 0.7f;
	private static final float SUBTITLE_SIZE_OF_TITLE = 0.6f;
	private static final float SUBTITLE_OFFSET_OF_TITLE = 0.9f;

	public byte[] watermark(final byte[] pdf) {
		try (final var document = PDDocument.load(pdf);
			final var out = new ByteArrayOutputStream()) {
			final var font = PDType0Font.load(document, fontSupplier(FONT).supply(), true);
			final var graphicsState = new PDExtendedGraphicsState();
			graphicsState.setNonStrokingAlphaConstant(OPACITY);

			for (final var page : document.getPages()) {
				stamp(document, page, font, graphicsState);
			}
			document.save(out);

			return out.toByteArray();
		} catch (final IOException e) {
			throw new TemplateException("Unable to watermark PDF", e);
		}
	}

	// Why: page /Rotate is ignored, since neither OpenHTMLtoPDF nor the Word converter sets it
	private static void stamp(final PDDocument document, final PDPage page, final PDFont font, final PDExtendedGraphicsState graphicsState) throws IOException {
		final var box = page.getCropBox();
		final var angle = Math.atan2(box.getHeight(), box.getWidth());
		final var titleSize = TITLE_WIDTH_OF_DIAGONAL * (float) Math.hypot(box.getWidth(), box.getHeight()) / textWidth(font, TITLE, 1);

		try (final var stream = new PDPageContentStream(document, page, APPEND, true, true)) {
			stream.setGraphicsStateParameters(graphicsState);
			stream.setNonStrokingColor(GRAY);
			stream.beginText();
			showCentered(stream, font, TITLE, titleSize, box, angle, 0);
			showCentered(stream, font, SUBTITLE, titleSize * SUBTITLE_SIZE_OF_TITLE, box, angle, -titleSize * SUBTITLE_OFFSET_OF_TITLE);
			stream.endText();
		}
	}

	private static void showCentered(final PDPageContentStream stream, final PDFont font, final String text, final float size,
		final PDRectangle box, final double angle, final float offsetY) throws IOException {
		final var matrix = Matrix.getRotateInstance(angle, box.getLowerLeftX() + box.getWidth() / 2, box.getLowerLeftY() + box.getHeight() / 2);
		matrix.translate(-textWidth(font, text, size) / 2, offsetY);

		stream.setFont(font, size);
		stream.setTextMatrix(matrix);
		stream.showText(text);
	}

	private static float textWidth(final PDFont font, final String text, final float size) throws IOException {
		return font.getStringWidth(text) / 1000 * size;
	}
}
