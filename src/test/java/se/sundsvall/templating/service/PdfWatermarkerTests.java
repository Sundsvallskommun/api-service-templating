package se.sundsvall.templating.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.templating.exception.TemplateException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.sundsvall.templating.service.PdfWatermarker.SUBTITLE;
import static se.sundsvall.templating.service.PdfWatermarker.TITLE;

class PdfWatermarkerTests {

	private final PdfWatermarker watermarker = new PdfWatermarker();

	@Test
	void watermark() throws IOException {
		final var result = watermarker.watermark(createPdf(PDRectangle.A4, "someContent"));

		try (final var pdf = PDDocument.load(result)) {
			assertThat(pdf.getNumberOfPages()).isOne();
			assertThat(text(pdf, 0)).contains("someContent", TITLE, SUBTITLE);
		}
	}

	@Test
	void watermark_withMultiplePages() throws IOException {
		final var result = watermarker.watermark(createPdf(PDRectangle.A4, "page1", "page2", "page3"));

		try (final var pdf = PDDocument.load(result)) {
			assertThat(pdf.getNumberOfPages()).isEqualTo(3);
			for (var i = 0; i < 3; i++) {
				assertThat(text(pdf, i)).contains("page" + (i + 1), TITLE, SUBTITLE);
			}
		}
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		false, true
	})
	void watermark_isPlacedWithinPage(final boolean landscape) throws IOException {
		final var size = landscape ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()) : PDRectangle.A4;

		final var result = watermarker.watermark(createPdf(size, "someContent"));

		try (final var pdf = PDDocument.load(result)) {
			assertThat(textPositions(pdf, 0)).allSatisfy(position -> {
				assertThat(position.getX()).isBetween(0f, size.getWidth());
				assertThat(position.getY()).isBetween(0f, size.getHeight());
			});
		}
	}

	@Test
	void watermark_withInvalidPdf() {
		final var notAPdf = "not a pdf".getBytes(UTF_8);

		assertThatExceptionOfType(TemplateException.class)
			.isThrownBy(() -> watermarker.watermark(notAPdf))
			.withMessage("Unable to watermark PDF");
	}

	private static byte[] createPdf(final PDRectangle size, final String... pageTexts) throws IOException {
		try (final var document = new PDDocument();
			final var out = new ByteArrayOutputStream()) {
			for (final var pageText : pageTexts) {
				final var page = new PDPage(size);
				document.addPage(page);
				try (final var stream = new PDPageContentStream(document, page)) {
					stream.beginText();
					stream.setFont(PDType1Font.HELVETICA, 12);
					stream.newLineAtOffset(50, 50);
					stream.showText(pageText);
					stream.endText();
				}
			}
			document.save(out);

			return out.toByteArray();
		}
	}

	// The stamp is rotated, so the glyphs are collected in content stream order rather than
	// relying on the line detection of PDFTextStripper
	private static String text(final PDDocument pdf, final int pageIndex) throws IOException {
		return textPositions(pdf, pageIndex).stream()
			.map(TextPosition::getUnicode)
			.collect(joining());
	}

	private static List<TextPosition> textPositions(final PDDocument pdf, final int pageIndex) throws IOException {
		final var positions = new ArrayList<TextPosition>();
		final var stripper = new PDFTextStripper() {

			@Override
			protected void processTextPosition(final TextPosition text) {
				positions.add(text);
			}
		};
		stripper.setStartPage(pageIndex + 1);
		stripper.setEndPage(pageIndex + 1);
		stripper.getText(pdf);

		return positions;
	}
}
