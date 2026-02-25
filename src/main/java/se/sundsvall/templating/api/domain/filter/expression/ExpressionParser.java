package se.sundsvall.templating.api.domain.filter.expression;

import se.sundsvall.templating.api.domain.filter.expression.logic.And;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;
import se.sundsvall.templating.api.domain.filter.expression.value.In;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.node.ArrayNode;

final class ExpressionParser extends StdDeserializer<Expression> {

	ExpressionParser() {
		super(Expression.class);
	}

	@Override
	public Expression deserialize(final JsonParser jsonParser, final DeserializationContext ctx) {
		JsonNode root;

		// The object was serialized as an embedded JSON string — re-parse it
		if (jsonParser.currentToken() == JsonToken.VALUE_STRING) {
			var rawJson = jsonParser.getString();

			root = ctx.readTree(ctx.createParser(rawJson));
		} else {
			root = ctx.readTree(jsonParser);
		}

		if (!root.isObject() || root.properties().isEmpty()) {
			return new Empty();
		}

		return parse(root);
	}

	Expression parse(final JsonNode node) {
		if (!node.isObject()) {
			throw new IllegalStateException("Object node expected");
		}

		if (node.properties().size() != 1) {
			throw new IllegalStateException("Node should contain a single entry");
		}

		final var firstChild = node.properties().iterator().next();
		final var name = firstChild.getKey();
		final var value = firstChild.getValue();

		return parseNode(name, value);
	}

	Expression parseNode(final String name, final JsonNode node) {
		return switch (name.toLowerCase()) {
			case "and" -> parseAnd(node);
			case "or" -> parseOr(node);
			case "not" -> parseNot(node);
			case "eq" -> parseEq(node);
			case "in" -> parseIn(node);
			default -> throw new IllegalStateException("Unknown expression: \"" + name + "\"");
		};
	}

	And parseAnd(final JsonNode node) {
		if (node instanceof ArrayNode arrayNode) {
			return new And(arrayNode.elements().stream().map(this::parse).toList());
		}

		throw new IllegalStateException("\"and\" node is not an array");
	}

	Or parseOr(final JsonNode node) {
		if (node instanceof ArrayNode arrayNode) {
			return new Or(arrayNode.elements().stream().map(this::parse).toList());
		}

		throw new IllegalStateException("\"or\" node is not an array");
	}

	Not parseNot(final JsonNode node) {
		return new Not(parse(node));
	}

	Eq parseEq(final JsonNode node) {
		if (node.properties().isEmpty()) {
			throw new IllegalStateException("\"eq\" node is empty");
		}

		final var content = node.properties().iterator().next();
		final var key = content.getKey();
		final var value = content.getValue().asString();

		return new Eq(key, value);
	}

	In parseIn(final JsonNode node) {
		if (node.properties().isEmpty()) {
			throw new IllegalStateException("\"in\" node is empty");
		}

		final var content = node.properties().iterator().next();
		final var key = content.getKey();
		final var values = content.getValue().values().stream()
			.map(JsonNode::asString)
			.toList();

		return new In(key, values);
	}
}
