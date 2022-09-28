package se.sundsvall.templating.exception;

public class TemplateException extends RuntimeException {

    public TemplateException(final String message) {
        super(message);
    }

    public TemplateException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public TemplateException(final Throwable cause) {
        super(cause);
    }
}
