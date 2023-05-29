package se.sundsvall.templating.api.domain.filter.expression.logic;

import java.util.List;

import se.sundsvall.templating.api.domain.filter.expression.Expression;

abstract class Junction implements Expression {

    protected final List<Expression> expressions;

    protected Junction(final List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }
}
