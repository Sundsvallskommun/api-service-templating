package se.sundsvall.templating.api.domain.filter.expression.logic;

import se.sundsvall.templating.api.domain.filter.expression.Expression;

public final class Not implements Expression {

    private final Expression expression;

    public Not(final Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return String.format("NOT %s", expression);
    }
}
