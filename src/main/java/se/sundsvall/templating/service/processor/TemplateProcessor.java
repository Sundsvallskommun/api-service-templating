package se.sundsvall.templating.service.processor;

import java.util.Map;

public interface TemplateProcessor<T> {

	byte[] process(T template, Map<String, Object> parameters);
}
