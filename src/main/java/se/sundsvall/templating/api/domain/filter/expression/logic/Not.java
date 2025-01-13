package se.sundsvall.templating.api.domain.filter.expression.logic;

import se.sundsvall.templating.api.domain.filter.expression.Expression;

public record Not(Expression expression)
	implements
	Expression {

	@Override
	public String toString() {
		return String.format("NOT %s", expression);
	}
}
