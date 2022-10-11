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
@Constraint(validatedBy = ValidTemplateIdValidator.class)
public @interface ValidTemplateId {

    String PATTERN = "[A-Za-z\\d\\-\\.]+$";

    String message() default "must be set and may only contain letters, digits, dashes and dots";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};
}
