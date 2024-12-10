package se.sundsvall.templating.api.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ValidTemplateVersionValidator implements ConstraintValidator<ValidTemplateVersion, String> {

	private static final Pattern PATTERN = Pattern.compile(ValidTemplateVersion.PATTERN);

	@Override
	public boolean isValid(final String s, final ConstraintValidatorContext context) {
		if (null == s) {
			return true;
		}

		return PATTERN.matcher(s).matches();
	}
}
