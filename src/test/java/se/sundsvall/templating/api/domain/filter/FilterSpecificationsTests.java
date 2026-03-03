package se.sundsvall.templating.api.domain.filter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.templating.api.domain.filter.expression.Empty;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.logic.And;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;
import se.sundsvall.templating.api.domain.filter.expression.value.In;
import se.sundsvall.templating.api.domain.filter.specification.ExpressionSpecification;
import se.sundsvall.templating.api.domain.filter.specification.logic.AndSpecification;
import se.sundsvall.templating.api.domain.filter.specification.logic.NotSpecification;
import se.sundsvall.templating.api.domain.filter.specification.logic.OrSpecification;
import se.sundsvall.templating.api.domain.filter.specification.value.EqSpecification;
import se.sundsvall.templating.api.domain.filter.specification.value.InSpecification;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterSpecificationsTests {

	static class Dummy {
	}

	static class DummyExpression implements Expression {
	}

	@ParameterizedTest
	@MethodSource("argumentsForToSpecification")
	void toSpecification(final Expression expression, final Class<ExpressionSpecification<Dummy>> expressionSpecificationClass) {
		assertThat(FilterSpecifications.toSpecification(Dummy.class, expression)).isInstanceOf(expressionSpecificationClass);
	}

	static Stream<Arguments> argumentsForToSpecification() {
		return Stream.of(
			argumentSet("AND", new And(emptyList()), AndSpecification.class),
			argumentSet("OR", new Or(emptyList()), OrSpecification.class),
			argumentSet("NOT", new Not(new Or(emptyList())), NotSpecification.class),
			argumentSet("EQ", new Eq("someKey", "someValue"), EqSpecification.class),
			argumentSet("IN", new In("someKey", List.of("someValue", "someOtherValue")), InSpecification.class),
			argumentSet("EMPTY", new Empty(), Specification.class));
	}

	@Test
	void toSpecificationForUnknownExpression() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> FilterSpecifications.toSpecification(Dummy.class, new DummyExpression()))
			.withMessageStartingWith("Unknown expression type");
	}

	@Test
	void createEmptySpecification() {
		var mockRoot = Mockito.<Root<Dummy>>mock();
		var mockQuery = Mockito.<CriteriaQuery<Dummy>>mock();
		var mockCriteriaBuilder = mock(CriteriaBuilder.class);
		var mockInnerQuery = Mockito.<CriteriaQuery<Dummy>>mock();
		var mockPredicate = mock(Predicate.class);

		when(mockCriteriaBuilder.createQuery(Dummy.class)).thenReturn(mockInnerQuery);
		when(mockInnerQuery.getRestriction()).thenReturn(mockPredicate);

		var spec = FilterSpecifications.createEmptySpecification(Dummy.class);
		var result = spec.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);

		assertThat(result).isSameAs(mockPredicate);
	}
}
