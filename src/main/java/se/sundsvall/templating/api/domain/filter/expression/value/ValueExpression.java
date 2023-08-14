package se.sundsvall.templating.api.domain.filter.expression.value;

import se.sundsvall.templating.api.domain.filter.expression.Expression;

public abstract class ValueExpression<T> implements Expression {

    protected String key;
    protected T value;

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
}
