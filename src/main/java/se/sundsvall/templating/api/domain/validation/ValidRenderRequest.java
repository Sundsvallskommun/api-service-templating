package se.sundsvall.templating.api.domain.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidRenderRequestValidator.class)
public @interface ValidRenderRequest {

    String message() default "exactly one of 'identifier' or 'metadata' must be set and non-null/empty";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default {};
}
