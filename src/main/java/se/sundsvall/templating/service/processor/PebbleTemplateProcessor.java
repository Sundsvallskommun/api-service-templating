package se.sundsvall.templating.service.processor;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.error.AttributeNotFoundException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.templating.exception.TemplateException;
import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Component
public class PebbleTemplateProcessor implements TemplateProcessor<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PebbleTemplateProcessor.class);

	private final PebbleEngine pebbleEngine;
	private final PebbleEngine strictPebbleEngine;

	public PebbleTemplateProcessor(@Qualifier("pebbleEngine") final PebbleEngine pebbleEngine,
		@Qualifier("strict-pebble-engine") final PebbleEngine strictPebbleEngine) {
		this.pebbleEngine = pebbleEngine;
		this.strictPebbleEngine = strictPebbleEngine;
	}

	@Override
	public byte[] process(final String template, final Map<String, Object> stringParameters) {
		return process(template, stringParameters, false);
	}

	public byte[] process(final String template, final Map<String, Object> stringParameters, final boolean strictParameters) {
		LOGGER.info("Processing Pebble template '{}'", sanitizeForLogging(template));
		Map<String, Object> parameters = new HashMap<>(stringParameters);
		final var engine = strictParameters ? strictPebbleEngine : pebbleEngine;
		try (var writer = new StringWriter()) {
			engine.getTemplate(template).evaluate(writer, parameters);

			var output = writer.toString();
			// Strip prefix from "direct" template processing output, if that's the case
			if (output.startsWith(DelegatingLoader.DIRECT_PREFIX)) {
				output = output.substring(DelegatingLoader.DIRECT_PREFIX.length());
			}

			return output.getBytes(UTF_8);
		} catch (final AttributeNotFoundException e) {
			throw Problem.valueOf(BAD_REQUEST, format("Missing template parameter '%s' (line %s)", e.getAttributeName(), e.getLineNumber()));
		} catch (final Exception e) {
			throw new TemplateException(e);
		}
	}
}
