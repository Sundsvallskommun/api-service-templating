package se.sundsvall.templating.service.processor;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class WordTemplateProcessorTests {

	private final WordTemplateProcessor processor = new WordTemplateProcessor();

	private static Stream<Arguments> sanitizeHtmlProvider() {
		return Stream.of(
			Arguments.of("escapes bare ampersand",
				"<p>Klös & Bit AB</p>",
				"<p>Klös &amp; Bit AB</p>"),
			Arguments.of("preserves already-escaped ampersand",
				"<p>Klös &amp; Bit AB</p>",
				"<p>Klös &amp; Bit AB</p>"),
			Arguments.of("preserves valid HTML structure",
				"<ul><li>Item 1</li><li>Item 2</li></ul>",
				"<ul><li>Item 1</li><li>Item 2</li></ul>"),
			Arguments.of("escapes bare left angle bracket",
				"<p>price < 100</p>",
				"<p>price &lt; 100</p>"),
			Arguments.of("converts HTML-only entities to XHTML-safe form",
				"<p>hello&nbsp;world &copy; 2026</p>",
				"<p>hello&#xa0;world \u00a9 2026</p>"),
			Arguments.of("self-closes void tags",
				"<p>line1<br>line2<hr></p>",
				"<p>line1<br />line2</p><hr /><p></p>"),
			Arguments.of("preserves pre block whitespace",
				"<pre>  line1\n  line2\n  line3</pre>",
				"<pre>  line1\n  line2\n  line3</pre>"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("sanitizeHtmlProvider")
	void sanitizeHtml(final String testName, final String input, final String expected) {
		final var result = processor.sanitizeHtml(input);
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void wrapHtml_producesExpectedWrapper() {
		final var input = "<p>Hello</p>";
		final var expected = """
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
			    <p>Hello</p>
			</body>
			</html>
			""";
		final var result = processor.wrapHtml(input);
		assertThat(result).isEqualTo(expected);
	}
}
