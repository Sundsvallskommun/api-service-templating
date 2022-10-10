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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import se.sundsvall.templating.integration.db.entity.TemplateEntity;

import lombok.Generated;
import lombok.Getter;

@Component
@Generated  // To skip coverage checks, since this class is WIP
public class TemplateProcessor {

    private static final Pattern FOR_PATTERN = Pattern.compile("\\[% for \\w+ in (\\w+) %\\]");

    private final PebbleEngine pebbleEngine;

    public TemplateProcessor(@Qualifier("debug-pebble-engine") final PebbleEngine pebbleEngine) {
        this.pebbleEngine = pebbleEngine;
    }

    public Set<TemplateVariable> getTemplateVars(final TemplateEntity templateEntity) {
        var vars = new HashSet<TemplateVariable>();
        var parameters = new HashMap<String, Object>();
        boolean exceptionCaught;

        var template = pebbleEngine.getTemplate(templateEntity.getIdentifier());

        do {
            exceptionCaught = false;
            try {
                template.evaluate(new StringWriter(), parameters);
            } catch (AttributeNotFoundException e) {
                exceptionCaught = true;

                var line = templateEntity.getContent().split("\n")[e.getLineNumber() - 1];
                var matcher = FOR_PATTERN.matcher(line);
                if (matcher.matches()) {
                    vars.add(new TemplateVariable(e.getAttributeName(), true));
                    parameters.put(e.getAttributeName(), List.of());
                } else {

                    vars.add(new TemplateVariable(e.getAttributeName()));
                    parameters.put(e.getAttributeName(), "");
                }
            } catch (PebbleException e) {
                e.printStackTrace(System.err);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to evaluate template '" + template.getName() + "'", e);
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
