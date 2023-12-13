package se.sundsvall.templating.exception;

public class TemplateException extends RuntimeException {

	private static final long serialVersionUID = 5401602756376588965L;

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
