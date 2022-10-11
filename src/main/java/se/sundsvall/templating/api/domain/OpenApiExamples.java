package se.sundsvall.templating.api.domain;

public final class OpenApiExamples {

    private OpenApiExamples() { }

    public static final String PARAMETERS = "{\"someKey\":\"someValue\",\"otherKey\":[\"otherValue1\",\"otherValue2\"],\"anotherKey\":{\"someKey\":\"someValue\"}}";

    public static final String UPDATE = "[{\"op\":\"add|remove|replace\",\"path\": \"/some/attribute/path\",\"value\": \"...\"}]";
}
