package se.sundsvall.templating.service.processor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.zalando.problem.Problem;

import static org.zalando.problem.Status.BAD_REQUEST;

final class HtmlValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(HtmlValidator.class);

	private static final Set<String> LIST_PARENT_TAGS = Set.of("ul", "ol");

	private HtmlValidator() {}

	/**
	 * Validate the given HTML fragment for issues that would cause docx4j to fail. Checks for:
	 * <ul>
	 * <li>Orphaned list items (li elements not inside ul/ol) - causes NPE in docx4j-ImportXHTML</li>
	 * <li>General HTML structural errors detected by the HTML5 parser</li>
	 * </ul>
	 *
	 * @param key  the parameter key (for error reporting)
	 * @param html the HTML fragment to validate
	 */
	static void validateHtml(final String key, final String html) {
		final var errors = new ArrayList<String>();

		checkOrphanedListItems(html, errors);
		checkHtmlStructure(html, errors);

		if (!errors.isEmpty()) {
			final var errorDetails = String.join("; ", errors);
			throw Problem.valueOf(BAD_REQUEST, "Invalid HTML in parameter '%s': %s".formatted(key, errorDetails));
		}
	}

	private static void checkOrphanedListItems(final String html, final List<String> errors) {
		final var document = Jsoup.parseBodyFragment(html);
		// Specifically check for <li> elements that are not inside <ul> or <ol>, as this is a common mistake that causes docx4j
		// to fail with an NPE.
		// Jsoup will parse the HTML and allow orphaned <li> elements, so we need to check this manually.
		for (final var li : document.body().select("li")) {
			final var parent = li.parent();
			if (parent != null && !LIST_PARENT_TAGS.contains(parent.tagName())) {
				errors.add("Element <li> is not allowed as a child of <%s>, must be inside <ul> or <ol>".formatted(parent.tagName()));
			}
		}
	}

	private static void checkHtmlStructure(final String html, final List<String> errors) {
		try {
			final var parser = new HtmlParser(XmlViolationPolicy.ALLOW);
			parser.setErrorHandler(new ErrorHandler() {

				@Override
				public void warning(final SAXParseException e) {
					// Ignore warnings
				}

				@Override
				public void error(final SAXParseException e) {
					errors.add(e.getMessage());
				}

				@Override
				public void fatalError(final SAXParseException e) {
					errors.add(e.getMessage());
				}
			});
			parser.setContentHandler(new DefaultHandler());
			parser.parseFragment(new InputSource(new StringReader(html)), "body");
		} catch (final Exception e) {
			LOGGER.warn("HTML structure check failed for key: {}", e.getMessage());
		}
	}
}
