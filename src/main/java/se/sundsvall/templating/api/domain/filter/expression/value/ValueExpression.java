package se.sundsvall.templating.api.domain.filter.expression.value;

import java.util.Objects;
import se.sundsvall.templating.api.domain.filter.expression.Expression;

public abstract class ValueExpression<T> implements Expression {

	protected final String key;
	protected final T value;

	protected ValueExpression(final String key, final T value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof ValueExpression<?> other)) {
			return false;
		}
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}
}
