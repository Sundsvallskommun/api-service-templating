package se.sundsvall.templating.domain;

import static java.lang.String.format;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KeyValue {

    @NotBlank
    private final String key;

    @NotBlank
    private final String value;

    @JsonCreator
    public static KeyValue of(@JsonProperty("key") final String key, @JsonProperty("value") final String value) {
        return new KeyValue(key, value);
    }

    @Override
    public String toString() {
        return format("{%s=%s}", key, value);
    }
}
