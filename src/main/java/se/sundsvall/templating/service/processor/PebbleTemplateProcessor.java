package se.sundsvall.templating.service.processor;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.pebbletemplates.pebble.PebbleEngine;
import java.io.StringWriter;
import java.util.Map;
import org.springframework.stereotype.Component;
import se.sundsvall.templating.exception.TemplateException;
import se.sundsvall.templating.service.pebble.loader.DelegatingLoader;

@Component
public class PebbleTemplateProcessor implements TemplateProcessor<String> {

	private final PebbleEngine pebbleEngine;

	public PebbleTemplateProcessor(final PebbleEngine pebbleEngine) {
		this.pebbleEngine = pebbleEngine;
	}

	@Override
	public byte[] process(final String template, final Map<String, Object> parameters) {
		try (var writer = new StringWriter()) {
			pebbleEngine.getTemplate(template).evaluate(writer, parameters);

			var output = writer.toString();
			// Strip prefix from "direct" template processing output, if that's the case
			if (output.startsWith(DelegatingLoader.DIRECT_PREFIX)) {
				output = output.substring(DelegatingLoader.DIRECT_PREFIX.length());
			}

			return output.getBytes(UTF_8);
		} catch (Exception e) {
			throw new TemplateException(e);
		}
	}
}
