package se.sundsvall.templating.api.domain.filter.expression;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Search filter expression", example = "{ \"or\": [ { \"eq\": { \"process\": \"PRH\" } }, { \"eq\": { \"verksamhet\": \"SBK\" } }] }")
@JsonDeserialize(using = ExpressionParser.class)
public interface Expression {

}