package se.sundsvall.templating.api.domain.validation;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import se.sundsvall.templating.api.domain.RenderRequest;

public class ValidRenderRequestValidator implements ConstraintValidator<ValidRenderRequest, RenderRequest> {

    @Override
    public boolean isValid(final RenderRequest request, final ConstraintValidatorContext context) {
        var identifierSet = isNotBlank(request.getIdentifier());
        var metadataSet = !isEmpty(request.getMetadata());

        return identifierSet ^ metadataSet;
    }
}
