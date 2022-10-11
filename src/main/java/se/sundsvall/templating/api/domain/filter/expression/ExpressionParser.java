package se.sundsvall.templating.api.domain.filter.expression;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import se.sundsvall.templating.api.domain.filter.expression.logic.And;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;
import se.sundsvall.templating.api.domain.filter.expression.value.In;
import se.sundsvall.templating.api.domain.filter.util.StreamUtil;

final class ExpressionParser extends StdDeserializer<Expression> {

    ExpressionParser() {
        super((Class<?>) null);
    }

    @Override
    public Expression deserialize(final JsonParser jsonParser, final DeserializationContext ctx)
            throws IOException {
        var root = (JsonNode) jsonParser.getCodec().readTree(jsonParser);

        if (!root.isObject() || StreamUtil.fromIterator(root.fields()).toList().isEmpty()) {
            return new EmptyExpression();
        }

        return parse(root);
    }

    Expression parse(final JsonNode node) {
        if (!node.isObject()) {
            throw new IllegalStateException("Object node expected");
        }

        var children = StreamUtil.fromIterator(node.fields()).toList();
        if (children.size() != 1) {
            throw new IllegalStateException("Node should contain a single entry");
        }

        var firstChild = children.get(0);
        var name = firstChild.getKey();
        var value = firstChild.getValue();

        return parseNode(name, value);
    }

    Expression parseNode(final String name, final JsonNode node) {
        return switch (name) {
            case "and" -> parseAnd(node);
            case "or" -> parseOr(node);
            case "not" -> parseNot(node);
            case "eq" -> parseEq(node);
            case "in" -> parseIn(node);
            default -> throw new IllegalStateException();
        };
    }

    And parseAnd(final JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalStateException("\"and\" node is not an array");
        }

        return new And(StreamUtil.fromIterator(node.elements()).map(this::parse).toList());
    }

    Or parseOr(final JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalStateException("\"or\" node is not an array");
        }

        return new Or(StreamUtil.fromIterator(node.elements()).map(this::parse).toList());
    }

    Not parseNot(final JsonNode node) {
        return new Not(parse(node));
    }

    Eq parseEq(final JsonNode node) {
        if (!node.fields().hasNext()) {
            throw new IllegalStateException("\"eq\" node is empty");
        }

        var content = node.fields().next();
        var key = content.getKey();
        var value = content.getValue().asText();

        return new Eq(key, value);
    }

    In parseIn(final JsonNode node) {
        if (!node.fields().hasNext()) {
            throw new IllegalStateException("\"in\" node is empty");
        }

        var content = node.fields().next();
        var key = content.getKey();
        var values = StreamUtil.fromIterator(content.getValue().elements()).map(JsonNode::asText).toList();

        return new In(key, values);
    }
}
