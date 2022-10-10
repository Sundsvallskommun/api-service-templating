package se.sundsvall.templating.api.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTemplateVersionValidator.class)
public @interface ValidTemplateVersion {

    String PATTERN = "^\\d+\\.\\d+$";

    String message() default "should be on the format major.minor, with both consisting of digits only";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};
}
