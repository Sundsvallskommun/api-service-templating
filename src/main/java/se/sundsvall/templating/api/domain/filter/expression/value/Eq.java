package se.sundsvall.templating.api.domain.filter.expression.value;

public final class Eq extends Value<String> {

    public Eq(final String key, final String value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return String.format("%s == %s", key, value);
    }
}
