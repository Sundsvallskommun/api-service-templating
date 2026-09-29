package se.sundsvall.templating.service.processor;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.StringLoader;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.templating.exception.TemplateException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class PebbleTemplateProcessorTests {

	private final PebbleTemplateProcessor processor = new PebbleTemplateProcessor(
		new PebbleEngine.Builder().loader(new StringLoader()).cacheActive(false).build(),
		new PebbleEngine.Builder().loader(new StringLoader()).cacheActive(false).strictVariables(true).build());

	@Test
	void process() {
		var result = processor.process("Hej {{ name }}", Map.of("name", "Bobby"));

		assertThat(new String(result, UTF_8)).isEqualTo("Hej Bobby");
	}

	@Test
	void process_withMissingParameter() {
		var result = processor.process("Hej {{ name }}{{ person.name }}", Map.of());

		assertThat(new String(result, UTF_8)).isEqualTo("Hej ");
	}

	@Test
	void process_strictWithAllParameters() {
		var result = processor.process("{{ name }} {{ person.name }}", Map.of("name", "Bobby", "person", Map.of("name", "Brun")), true);

		assertThat(new String(result, UTF_8)).isEqualTo("Bobby Brun");
	}

	@Test
	void process_strictWithMissingRootParameter() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> processor.process("Hej {{ name }}", Map.of(), true))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).isEqualTo("Missing template parameter 'name' (line 1)");
			});
	}

	@Test
	void process_strictWithMissingNestedParameter() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> processor.process("Hej\n{{ person.name }}", Map.of("person", Map.of("street", "Storgatan 2")), true))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).isEqualTo("Missing template parameter 'name' (line 2)");
			});
	}

	@Test
	void process_strictWithMissingParameterInComparison() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> processor.process("{% if status == \"x\" %}X{% endif %}", Map.of(), true))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).isEqualTo("Missing template parameter 'status' (line 1)");
			});
	}

	@Test
	void process_strictWithMissingParameterInArithmetic() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> processor.process("{{ count + 1 }}", Map.of(), true))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).isEqualTo("Missing template parameter 'count' (line 1)");
			});
	}

	@Test
	void process_strictWithMissingParameterInIncludedTemplate() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> processor.process("Hej {% include \"{{ name }}\" %}", Map.of(), true))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).isEqualTo("Missing template parameter 'name' (line 1 in template '{{ name }}')");
			});
	}

	@Test
	void process_strictWithMissingConditionParameter() {
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> processor.process("{% if checked %}X{% endif %}", Map.of(), true))
			.satisfies(problem -> {
				assertThat(problem.getStatus()).isEqualTo(BAD_REQUEST);
				assertThat(problem.getDetail()).isEqualTo("Missing template parameter 'checked' (line 1)");
			});
	}

	@Test
	void process_strictWithMissingConditionParameterAndDefaultFilter() {
		var result = processor.process("{% if checked | default(false) %}X{% endif %}", Map.of(), true);

		assertThat(new String(result, UTF_8)).isEmpty();
	}

	@Test
	void process_strictWithDefaultFilter() {
		var result = processor.process("{{ name | default(\"Bobby\") }}{{ person.name | default(\"\") }}", Map.of(), true);

		assertThat(new String(result, UTF_8)).isEqualTo("Bobby");
	}

	@Test
	void process_withInvalidTemplate() {
		assertThatExceptionOfType(TemplateException.class)
			.isThrownBy(() -> processor.process("{% if %}", Map.of()));
	}
}
