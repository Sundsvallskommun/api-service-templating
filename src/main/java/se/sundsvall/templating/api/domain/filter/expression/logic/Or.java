package se.sundsvall.templating.api.domain.filter.expression.logic;

import static java.util.stream.Collectors.joining;

import java.util.List;

import se.sundsvall.templating.api.domain.filter.expression.Expression;

public class Or extends Junction {

    public Or(final List<Expression> expressions) {
        super(expressions);
    }

    @Override
    public String toString() {
        return expressions.stream().map(Expression::toString).collect(joining(" OR ", "(", ")"));
    }
}
