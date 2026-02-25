package se.sundsvall.templating.api.domain.filter.expression;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.templating.api.domain.filter.expression.logic.And;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;
import se.sundsvall.templating.api.domain.filter.expression.value.In;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static tools.jackson.core.JsonToken.START_OBJECT;
import static tools.jackson.core.JsonToken.VALUE_STRING;

@ExtendWith(MockitoExtension.class)
class ExpressionParserTests {

	private final ExpressionParser parser = new ExpressionParser();

	@Test
	void deserializeWhenSerializedAsJsonString() {
		var key = "someKey";
		var value = "someValue";
		var rawJson = """
			{"eq":{"field":"value"}}
			""";

		var mockJsonParser = mock(JsonParser.class);
		var mockContext = mock(DeserializationContext.class);
		var mockInnerParser = mock(JsonParser.class);
		var mockRootNode = mock(ObjectNode.class);
		var mockEntryNode = mock(ObjectNode.class);
		var mockValueNode = mock(StringNode.class);
		var entry = Map.<String, JsonNode>entry("eq", mockEntryNode);

		when(mockJsonParser.currentToken()).thenReturn(VALUE_STRING);
		when(mockJsonParser.getString()).thenReturn(rawJson);
		when(mockContext.createParser(rawJson)).thenReturn(mockInnerParser);
		when(mockContext.readTree(mockInnerParser)).thenReturn(mockRootNode);
		when(mockRootNode.isObject()).thenReturn(true);
		when(mockRootNode.properties()).thenReturn(Set.of(entry));
		when(mockEntryNode.properties()).thenReturn(Set.of(Map.entry(key, mockValueNode)));
		when(mockValueNode.asString()).thenReturn(value);

		var result = parser.deserialize(mockJsonParser, mockContext);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(Eq.class))
			.satisfies(eq -> {
				assertThat(eq.getKey()).isEqualTo(key);
				assertThat(eq.getValue()).isEqualTo(value);
			});
	}

	@Test
	void deserialize() {
		var key = "someKey";
		var value = "someValue";

		var mockJsonParser = mock(JsonParser.class);
		var mockContext = mock(DeserializationContext.class);
		var mockRootNode = mock(ObjectNode.class);
		var mockValueNode = mock(StringNode.class);
		var entry = Map.entry("eq", (JsonNode) mock(ObjectNode.class));

		when(mockJsonParser.currentToken()).thenReturn(START_OBJECT);
		when(mockContext.readTree(mockJsonParser)).thenReturn(mockRootNode);
		when(mockRootNode.isObject()).thenReturn(true);
		when(mockRootNode.properties()).thenReturn(Set.of(entry));

		var innerNode = (ObjectNode) entry.getValue();
		var fieldEntry = Map.<String, JsonNode>entry(key, mockValueNode);
		when(innerNode.properties()).thenReturn(Set.of(fieldEntry));
		when(mockValueNode.asString()).thenReturn(value);

		var result = parser.deserialize(mockJsonParser, mockContext);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(Eq.class))
			.satisfies(eq -> {
				assertThat(eq.getKey()).isEqualTo(key);
				assertThat(eq.getValue()).isEqualTo(value);
			});
	}

	@Test
	void deserializeWhenRootIsNotAnObjectNode() {
		var mockJsonParser = mock(JsonParser.class);
		var mockContext = mock(DeserializationContext.class);
		var mockRootNode = mock(JsonNode.class);

		when(mockJsonParser.currentToken()).thenReturn(START_OBJECT);
		when(mockContext.readTree(mockJsonParser)).thenReturn(mockRootNode);
		when(mockRootNode.isObject()).thenReturn(false);

		var result = parser.deserialize(mockJsonParser, mockContext);

		assertThat(result).isInstanceOf(Empty.class);
	}

	@Test
	void deserializeWhenRootIsAndEmptyObjectNode() {
		var mockJsonParser = mock(JsonParser.class);
		var mockContext = mock(DeserializationContext.class);
		var mockRootNode = mock(ObjectNode.class);

		when(mockJsonParser.currentToken()).thenReturn(START_OBJECT);
		when(mockContext.readTree(mockJsonParser)).thenReturn(mockRootNode);
		when(mockRootNode.isObject()).thenReturn(true);
		when(mockRootNode.properties()).thenReturn(Set.of());

		var result = parser.deserialize(mockJsonParser, mockContext);

		assertThat(result).isInstanceOf(Empty.class);
	}

	@Test
	void parseWhenNodeIsNotAnObjectNode() {
		var node = mock(JsonNode.class);
		when(node.isObject()).thenReturn(false);

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parse(node))
			.withMessage("Object node expected");
	}

	@Test
	void parseWhenNodeHasMoreThanOneEntry() {
		var mockNode = mock(ObjectNode.class);
		when(mockNode.isObject()).thenReturn(true);
		when(mockNode.properties()).thenReturn(Set.of(
			Map.entry("eq", mock(JsonNode.class)),
			Map.entry("or", mock(JsonNode.class))));

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parse(mockNode))
			.withMessage("Node should contain a single entry");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"and", "or", "not", "eq", "in"
	})
	void parseNodeWithKnownName(final String name) {
		var mockNode = mock(JsonNode.class);
		var mockParser = mock(ExpressionParser.class);
		when(mockParser.parseNode(name, mockNode)).thenCallRealMethod();

		mockParser.parseNode(name, mockNode);

		switch (name) {
			case "and" -> verify(mockParser).parseAnd(mockNode);
			case "or" -> verify(mockParser).parseOr(mockNode);
			case "not" -> verify(mockParser).parseNot(mockNode);
			case "eq" -> verify(mockParser).parseEq(mockNode);
			case "in" -> verify(mockParser).parseIn(mockNode);
		}
		verifyNoMoreInteractions(mockParser);
		verifyNoInteractions(mockNode);
	}

	@Test
	void parseNodeWithUnknownName() {
		var mockNode = mock(JsonNode.class);

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parseNode("unknown", mockNode))
			.withMessageStartingWith("Unknown expression");
	}

	@Test
	void parseNodeIsCaseInsensitive() {
		var mockArrayNode = mock(ArrayNode.class);
		when(mockArrayNode.elements()).thenReturn(emptyList());

		var result = parser.parseNode("AND", mockArrayNode);

		assertThat(result).isInstanceOf(And.class);
	}

	@Test
	void parseAnd() {
		var mockEqNode1 = mock(ObjectNode.class);
		when(mockEqNode1.isObject()).thenReturn(true);
		when(mockEqNode1.properties()).thenReturn(Set.of(Map.entry("eq", mock(ObjectNode.class))));

		var mockEqNode2 = mock(ObjectNode.class);
		when(mockEqNode2.isObject()).thenReturn(true);
		when(mockEqNode2.properties()).thenReturn(Set.of(Map.entry("eq", mock(ObjectNode.class))));

		var mockChildNode3 = mock(StringNode.class);
		when(mockChildNode3.asString()).thenReturn("someValue");

		var mockChildNode4 = mock(StringNode.class);
		when(mockChildNode4.asString()).thenReturn("someOtherValue");

		var mockEq1Inner = (ObjectNode) mockEqNode1.properties().iterator().next().getValue();
		when(mockEq1Inner.properties()).thenReturn(Set.of(Map.entry("someKey", mockChildNode3)));

		var mockEq2Inner = (ObjectNode) mockEqNode2.properties().iterator().next().getValue();
		when(mockEq2Inner.properties()).thenReturn(Set.of(Map.entry("someOtherKey", mockChildNode4)));

		var arrayNode = mock(ArrayNode.class);
		when(arrayNode.elements()).thenReturn(List.of(mockEqNode1, mockEqNode2));

		var result = parser.parseAnd(arrayNode);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(And.class))
			.satisfies(and -> assertThat(and.getExpressions()).hasSize(2));
	}

	@Test
	void parseAndWhenNodeIsNotAnArrayNode() {
		var mockNode = mock(JsonNode.class);

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parseAnd(mockNode))
			.withMessageStartingWith("\"and\" node is not an array");
	}

	@Test
	void parseOr() {
		var mockEqNode1 = mock(ObjectNode.class);
		when(mockEqNode1.isObject()).thenReturn(true);
		when(mockEqNode1.properties()).thenReturn(Set.of(Map.entry("eq", mock(ObjectNode.class))));

		var mockEqNode2 = mock(ObjectNode.class);
		when(mockEqNode2.isObject()).thenReturn(true);
		when(mockEqNode2.properties()).thenReturn(Set.of(Map.entry("eq", mock(ObjectNode.class))));

		var mockChildNode3 = mock(StringNode.class);
		when(mockChildNode3.asString()).thenReturn("someValue");

		var mockChildNode4 = mock(StringNode.class);
		when(mockChildNode4.asString()).thenReturn("someOtherValue");

		var mockEq1Inner = (ObjectNode) mockEqNode1.properties().iterator().next().getValue();
		when(mockEq1Inner.properties()).thenReturn(Set.of(Map.entry("someKey", mockChildNode3)));

		var mockEq2Inner = (ObjectNode) mockEqNode2.properties().iterator().next().getValue();
		when(mockEq2Inner.properties()).thenReturn(Set.of(Map.entry("someOtherKey", mockChildNode4)));

		var mockArrayNode = mock(ArrayNode.class);
		when(mockArrayNode.elements()).thenReturn(List.of(mockEqNode1, mockEqNode2));

		var result = parser.parseOr(mockArrayNode);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(Or.class))
			.satisfies(or -> assertThat(or.getExpressions()).hasSize(2));
	}

	@Test
	void parseOrWhenNodeIsNotAnArrayNode() {
		var mockNode = mock(JsonNode.class);

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parseOr(mockNode))
			.withMessageStartingWith("\"or\" node is not an array");
	}

	@Test
	void parseNot() {
		var mockNode = mock(ObjectNode.class);
		when(mockNode.isObject()).thenReturn(true);
		when(mockNode.properties()).thenReturn(Set.of(Map.entry("eq", mock(ObjectNode.class))));

		var mockValueNode = mock(StringNode.class);
		when(mockValueNode.asString()).thenReturn("someValue");

		var eqNode = (ObjectNode) mockNode.properties().iterator().next().getValue();
		when(eqNode.properties()).thenReturn(Set.of(Map.entry("someKey", mockValueNode)));

		var result = parser.parseNot(mockNode);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(Not.class))
			.satisfies(not -> assertThat(not.expression()).isInstanceOf(Eq.class));
	}

	@Test
	void parseEq() {
		var key = "someKey";
		var value = "someValue";

		var mockValueNode = mock(StringNode.class);
		when(mockValueNode.asString()).thenReturn(value);

		var mockNode = mock(ObjectNode.class);
		when(mockNode.properties()).thenReturn(Set.of(Map.entry(key, mockValueNode)));

		var result = parser.parseEq(mockNode);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(Eq.class))
			.satisfies(eq -> {
				assertThat(eq.getKey()).isEqualTo(key);
				assertThat(eq.getValue()).isEqualTo(value);
			});
	}

	@Test
	void parseEqWhenNodeIsEmpty() {
		var mockNode = mock(ObjectNode.class);
		when(mockNode.properties()).thenReturn(Set.of());

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parseEq(mockNode))
			.withMessageStartingWith("\"eq\" node is empty");
	}

	@Test
	void parseIn() {
		var key = "someKey";
		var value1 = "someValue";
		var value2 = "someOtherValue";

		var mockValueNode1 = mock(StringNode.class);
		when(mockValueNode1.asString()).thenReturn(value1);

		var mockValueNode2 = mock(StringNode.class);
		when(mockValueNode2.asString()).thenReturn(value2);

		var mockValuesNode = mock(ArrayNode.class);
		when(mockValuesNode.values()).thenReturn(List.of(mockValueNode1, mockValueNode2));

		var mockNode = mock(ObjectNode.class);
		when(mockNode.properties()).thenReturn(Set.of(Map.entry(key, mockValuesNode)));

		var result = parser.parseIn(mockNode);

		assertThat(result)
			.asInstanceOf(InstanceOfAssertFactories.type(In.class))
			.satisfies(in -> {
				assertThat(in.getKey()).isEqualTo(key);
				assertThat(in.getValue()).containsExactlyInAnyOrder(value1, value2);
			});
	}

	@Test
	void parseInWhenNodeIsEmpty() {
		var mockNode = mock(ObjectNode.class);
		when(mockNode.properties()).thenReturn(Set.of());

		assertThatExceptionOfType(IllegalStateException.class)
			.isThrownBy(() -> parser.parseIn(mockNode))
			.withMessageStartingWith("\"in\" node is empty");
	}
}
