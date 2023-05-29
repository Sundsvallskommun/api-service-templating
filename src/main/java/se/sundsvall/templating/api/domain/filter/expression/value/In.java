package se.sundsvall.templating.api.domain.filter.expression.value;

import java.util.List;

public final class In extends Value<List<String>> {

    public In(final String key, final List<String> value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return String.format("%s IN %s", key, value);
    }
}
