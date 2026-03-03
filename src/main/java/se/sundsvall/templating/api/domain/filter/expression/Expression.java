package se.sundsvall.templating.api.domain.filter.expression;

import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.databind.annotation.JsonDeserialize;

@Schema(description = "Search filter expression", examples = "{ \"or\": [ { \"eq\": { \"process\": \"PRH\" } }, { \"eq\": { \"verksamhet\": \"SBK\" } }] }")
@JsonDeserialize(using = ExpressionParser.class)
public interface Expression {

}
