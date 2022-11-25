package se.sundsvall.templating.service.pebble.processor;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.AttributeNotFoundException;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.loader.StringLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import se.sundsvall.templating.configuration.properties.PebbleProperties;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.util.BASE64;

import lombok.Generated;
import lombok.Getter;

@Component
@Generated  // To skip coverage checks, since this class is WIP
public class TemplateProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateProcessor.class);

    private static final Pattern FOR_PATTERN = Pattern.compile("\\{%\\s+for\\s+\\w+\\s+in\\s+(\\w+)\\s+%}");
    private static final Pattern IF_PATTERN = Pattern.compile("\\{%\\s+if\\s+(\\w+).*\\s+%}");

    private final PebbleEngine pebbleEngine;
    private final PebbleProperties pebbleProperties;

    public TemplateProcessor() {
        pebbleEngine = new PebbleEngine.Builder()
            .loader(new StringLoader())
            .autoEscaping(false)
            .strictVariables(true)
            .build();
        pebbleProperties = null;
    }

    public TemplateProcessor(@Qualifier("debug-pebble-engine") final PebbleEngine pebbleEngine,
            final PebbleProperties pebbleProperties) {
        this.pebbleEngine = pebbleEngine;
        this.pebbleProperties = pebbleProperties;
    }

    public void processTemplate(final TemplateEntity templateEntity) {
        var content = BASE64.decode(templateEntity.getContent());

        var print = pebbleProperties.getDelimiters().getPrint();

    }

    public Set<TemplateVariable> getTemplateVars(final String template) {
        return getTemplateVarsInternal(template);
    }

    public Set<TemplateVariable> getTemplateVars(final TemplateEntity templateEntity) {
        return getTemplateVarsInternal(templateEntity.getIdentifier());
    }

    private Set<TemplateVariable> getTemplateVarsInternal(final String template) {
        var vars = new HashSet<TemplateVariable>();
        var parameters = new HashMap<String, Object>();
        boolean exceptionCaught;

        var pebbleTemplate = pebbleEngine.getTemplate(template);
        do {
            exceptionCaught = false;
            try {
                pebbleTemplate.evaluate(new StringWriter(), parameters);
            } catch (AttributeNotFoundException e) {
                exceptionCaught = true;

                var templateVariable = new TemplateVariable(e.getAttributeName());
                LOG.info("Found simple template variable '{}'", templateVariable);
                vars.add(templateVariable);

                parameters.put(templateVariable.name, "");
            } catch (PebbleException e) {
                exceptionCaught = true;

                var line = template.split("\n")[e.getLineNumber() - 1];
                var matcher = FOR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    var variableName = matcher.group(1);
                    LOG.info("Replacing simple template variable '{}' with collection", variableName);

                    vars.removeIf(templateVariable -> templateVariable.name.equals(variableName));
                    vars.add(new TemplateVariable(variableName, true));

                    parameters.put(variableName, List.of());
                } else {
                    matcher = IF_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        var variableName = matcher.group(1);
                        LOG.info("Replacing simple template variable '{}' with collection", variableName);

                        vars.removeIf(templateVariable -> templateVariable.name.equals(variableName));
                        vars.add(new TemplateVariable(variableName, true));

                        parameters.put(variableName, List.of());
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unable to evaluate template", e);
            }
        } while (exceptionCaught);

        return vars;
    }

    @Getter
    @Generated  // To skip coverage checks, since this class is WIP
    public static class TemplateVariable {

        private final String name;
        private final boolean collection;

        TemplateVariable(String name) {
            this(name, false);
        }

        TemplateVariable(String name, boolean collection) {
            this.name = name;
            this.collection = collection;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            var other = (TemplateVariable) o;
            return name.equals(other.name);
        }

        @Override
        public String toString() {
            var sb = new StringBuilder(name);

            if (collection) {
                sb.append(" (collection)");
            }

            return sb.toString();
        }
    }
}
