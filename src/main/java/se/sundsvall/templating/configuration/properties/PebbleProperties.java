package se.sundsvall.templating.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "pebble")
public class PebbleProperties {

    private boolean useCaseInsensitiveKeys = true;
    private boolean autoEscape = false;
    private Delimiters delimiters = new Delimiters();

    @Getter
    @Setter
    public static final class Delimiters {

        private Print print = new Print();
        private Comment comment = new Comment();
        private Execute execute = new Execute();
        private Interpolation interpolation = new Interpolation();
    }

    @Getter
    @Setter
    public static class Print {

        private String open = "{{";
        private String close = "}}";
    }

    @Getter
    @Setter
    public static final class Comment {

        private String open = "{#";
        private String close = "#}";
    }

    @Getter
    @Setter
    public static final class Execute {

        private String open = "{%";
        private String close = "%}";
    }

    @Getter
    @Setter
    public static final class Interpolation {

        private String open = "#{";
        private String close = "}";
    }
}
