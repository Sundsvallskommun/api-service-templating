package se.sundsvall.templating.service.processor;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zalando.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.zalando.problem.Status.BAD_REQUEST;

class HtmlValidatorTests {

	private static Stream<Arguments> invalidHtmlProvider() {
		return Stream.of(
			Arguments.of("test-param", "<li>orphaned item</li>"),
			Arguments.of("bad-nesting", "<p><div>div inside p</div></p>"));
	}

	private static Stream<Arguments> validHtmlProvider() {
		return Stream.of(
			Arguments.of("<ul><li>valid item</li></ul>"),
			Arguments.of("<p>Simple paragraph</p>"));
	}

	@ParameterizedTest
	@MethodSource("invalidHtmlProvider")
	void validateHtml_invalidHtml_throwsBadRequest(final String key, final String html) {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> HtmlValidator.validateHtml(key, html))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).contains(key);
			});
	}

	@ParameterizedTest
	@MethodSource("validHtmlProvider")
	void validateHtml_validHtml_passes(final String html) {
		assertThatNoException()
			.isThrownBy(() -> HtmlValidator.validateHtml("test-param", html));
	}
}
