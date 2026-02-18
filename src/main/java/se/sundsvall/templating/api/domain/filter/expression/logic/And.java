package se.sundsvall.templating.api.domain.filter.expression.logic;

import java.util.List;
import se.sundsvall.templating.api.domain.filter.expression.Expression;

import static java.util.stream.Collectors.joining;

public class And extends Junction {

	public And(final List<Expression> expressions) {
		super(expressions);
	}

	@Override
	public String toString() {
		return expressions.stream().map(Expression::toString).collect(joining(" AND ", "(", ")"));
	}
}
