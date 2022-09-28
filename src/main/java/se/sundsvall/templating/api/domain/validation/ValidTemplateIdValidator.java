package se.sundsvall.templating.api.domain.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidTemplateIdValidator implements ConstraintValidator<ValidTemplateId, String> {

    private static final Pattern PATTERN = Pattern.compile(ValidTemplateId.PATTERN);

    @Override
    public boolean isValid(final String s, final ConstraintValidatorContext context) {
        if (null == s) {
            return false;
        }

        return PATTERN.matcher(s).matches();
    }
}
