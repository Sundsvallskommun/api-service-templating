package se.sundsvall.templating.api.domain.filter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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
import se.sundsvall.templating.integration.db.entity.MetadataEntity;
import se.sundsvall.templating.integration.db.entity.MetadataEntity_;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity_;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataFilterSpecificationsTests {

	static class DummyExpression implements Expression {
	}

	@ParameterizedTest
	@MethodSource("argumentsForToSpecification")
	void toSpecification(final Expression expression, final Class<ExpressionSpecification<TemplateEntity>> expressionSpecificationClass) {
		assertThat(MetadataFilterSpecifications.toSpecification(TemplateEntity.class, expression)).isInstanceOf(expressionSpecificationClass);
	}

	static Stream<Arguments> argumentsForToSpecification() {
		return Stream.of(
			argumentSet("AND", new And(emptyList()), AndSpecification.class),
			argumentSet("OR", new Or(emptyList()), OrSpecification.class),
			argumentSet("NOT", new Not(new Or(emptyList())), NotSpecification.class),
			argumentSet("EQ", new Eq("someKey", "someValue"), MetadataFilterSpecifications.MetadataEqSpecification.class),
			argumentSet("IN", new In("someKey", List.of("someValue", "someOtherValue")), MetadataFilterSpecifications.MetadataInSpecification.class),
			argumentSet("EMPTY", new Empty(), Specification.class));
	}

	@Test
	void toSpecificationForUnknownExpression() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> MetadataFilterSpecifications.toSpecification(TemplateEntity.class, new DummyExpression()))
			.withMessageStartingWith("Unknown expression type");
	}

	@Nested
	class MetadataEqSpecificationTests {

		@Mock
		private Root<TemplateEntity> mockRoot;
		@Mock
		private CriteriaQuery<TemplateEntity> mockQuery;
		@Mock
		private CriteriaBuilder mockCriteriaBuilder;
		@Mock
		private ListJoin<TemplateEntity, MetadataEntity> mockJoin;
		@Mock
		private jakarta.persistence.criteria.Expression<List<MetadataEntity>> mockMetadataExpression;
		@Mock
		private Predicate mockIsMemberPredicate;
		@Mock
		private SingularAttribute<MetadataEntity, String> mockKeyAttribute;
		@Mock
		private SingularAttribute<MetadataEntity, String> mockValueAttribute;
		@Mock
		private Path<String> mockJoinKeyPath;
		@Mock
		private Path<String> mockJoinValuePath;
		@Mock
		private Predicate mockAndPredicate;
		@Mock
		private Predicate mockKeyEqualPredicate;
		@Mock
		private Predicate mockValueEqualPredicate;
		@Mock
		private Predicate mockResultingPredicate;

		@Test
		void constructorAndGetters() {
			var eq = new Eq("someKey", "someValue");
			var eqSpecification = new MetadataFilterSpecifications.MetadataEqSpecification(eq);

			assertThat(eqSpecification.getExpression()).isEqualTo(eq);
		}

		@Test
		void toPredicate() {
			MetadataEntity_.key = mockKeyAttribute;
			MetadataEntity_.value = mockValueAttribute;

			var eq = new Eq("someKey", "someValue");
			var eqSpecification = new MetadataFilterSpecifications.MetadataEqSpecification(eq);

			when(mockRoot.join(TemplateEntity_.metadata, JoinType.LEFT)).thenReturn(mockJoin);

			when(mockJoin.get(mockKeyAttribute)).thenReturn(mockJoinKeyPath);
			when(mockJoin.get(mockValueAttribute)).thenReturn(mockJoinValuePath);

			when(mockRoot.get(TemplateEntity_.metadata)).thenReturn(mockMetadataExpression);
			when(mockCriteriaBuilder.isMember(mockJoin, mockMetadataExpression)).thenReturn(mockIsMemberPredicate);

			when(mockCriteriaBuilder.equal(mockJoinKeyPath, eq.getKey())).thenReturn(mockKeyEqualPredicate);
			when(mockCriteriaBuilder.equal(mockJoinValuePath, eq.getValue())).thenReturn(mockValueEqualPredicate);
			when(mockCriteriaBuilder.and(mockKeyEqualPredicate, mockValueEqualPredicate)).thenReturn(mockAndPredicate);

			when(mockQuery.where(mockIsMemberPredicate, mockAndPredicate)).thenReturn(mockQuery);
			when(mockQuery.getRestriction()).thenReturn(mockResultingPredicate);

			var result = eqSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			assertThat(result).isEqualTo(mockResultingPredicate);

			verify(mockRoot).join(TemplateEntity_.metadata, JoinType.LEFT);
			verify(mockRoot).get(TemplateEntity_.metadata);
			verify(mockJoin).get(mockKeyAttribute);
			verify(mockJoin).get(mockValueAttribute);
			verify(mockCriteriaBuilder).isMember(mockJoin, mockMetadataExpression);
			verify(mockCriteriaBuilder).equal(mockJoinKeyPath, eq.getKey());
			verify(mockCriteriaBuilder).equal(mockJoinValuePath, eq.getValue());
			verify(mockCriteriaBuilder).and(mockKeyEqualPredicate, mockValueEqualPredicate);
			verify(mockQuery).where(mockIsMemberPredicate, mockAndPredicate);
			verify(mockQuery).getRestriction();
			verifyNoMoreInteractions(mockRoot, mockJoin, mockCriteriaBuilder, mockQuery);
			verifyNoInteractions(mockResultingPredicate);
		}
	}

	@Nested
	class MetadataInSpecificationTests {

		@Test
		void constructorAndGetters() {
			var in = new In("someKey", List.of("someValue", "someOtherValue"));
			var inSpecification = new MetadataFilterSpecifications.MetadataInSpecification(in);

			assertThat(inSpecification.getExpression()).isEqualTo(in);
		}

		@Mock
		private Root<TemplateEntity> mockRoot;
		@Mock
		private CriteriaQuery<TemplateEntity> mockQuery;
		@Mock
		private CriteriaBuilder mockCriteriaBuilder;
		@Mock
		private ListJoin<TemplateEntity, MetadataEntity> mockJoin;
		@Mock
		private jakarta.persistence.criteria.Expression<List<MetadataEntity>> mockMetadataExpression;
		@Mock
		private Predicate mockIsMemberPredicate;
		@Mock
		private SingularAttribute<MetadataEntity, String> mockKeyAttribute;
		@Mock
		private SingularAttribute<MetadataEntity, String> mockValueAttribute;
		@Mock
		private Path<String> mockJoinKeyPath;
		@Mock
		private Path<String> mockJoinValuePath;
		@Mock
		private Predicate mockAndPredicate;
		@Mock
		private Predicate mockKeyEqualPredicate;
		@Mock
		private Predicate mockResultingPredicate;

		@Mock
		private CriteriaBuilder.In<String> mockValueInExpression;

		@Test
		void toPredicate() {
			MetadataEntity_.key = mockKeyAttribute;
			MetadataEntity_.value = mockValueAttribute;

			var in = new In("someKey", List.of("someValue", "someOtherValue"));
			var inSpecification = new MetadataFilterSpecifications.MetadataInSpecification(in);

			when(mockRoot.join(TemplateEntity_.metadata, JoinType.LEFT)).thenReturn(mockJoin);
			when(mockJoin.get(mockKeyAttribute)).thenReturn(mockJoinKeyPath);
			when(mockJoin.get(mockValueAttribute)).thenReturn(mockJoinValuePath);
			when(mockCriteriaBuilder.in(mockJoinValuePath)).thenReturn(mockValueInExpression);

			when(mockRoot.get(TemplateEntity_.metadata)).thenReturn(mockMetadataExpression);
			when(mockCriteriaBuilder.isMember(mockJoin, mockMetadataExpression)).thenReturn(mockIsMemberPredicate);
			when(mockCriteriaBuilder.equal(mockJoinKeyPath, in.getKey())).thenReturn(mockKeyEqualPredicate);
			when(mockCriteriaBuilder.and(mockKeyEqualPredicate, mockValueInExpression)).thenReturn(mockAndPredicate);

			when(mockQuery.where(mockIsMemberPredicate, mockAndPredicate)).thenReturn(mockQuery);
			when(mockQuery.getRestriction()).thenReturn(mockResultingPredicate);

			var result = inSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			assertThat(result).isEqualTo(mockResultingPredicate);

			verify(mockRoot).join(TemplateEntity_.metadata, JoinType.LEFT);
			verify(mockRoot).get(TemplateEntity_.metadata);
			verify(mockJoin).get(mockKeyAttribute);
			verify(mockJoin).get(mockValueAttribute);
			verify(mockCriteriaBuilder).in(mockJoinValuePath);
			verify(mockCriteriaBuilder).isMember(mockJoin, mockMetadataExpression);
			verify(mockCriteriaBuilder).equal(mockJoinKeyPath, in.getKey());
			verify(mockCriteriaBuilder).and(mockKeyEqualPredicate, mockValueInExpression);
			verify(mockQuery).where(mockIsMemberPredicate, mockAndPredicate);
			verify(mockQuery).getRestriction();
			verifyNoMoreInteractions(mockRoot, mockJoin, mockCriteriaBuilder, mockQuery);
			verifyNoInteractions(mockResultingPredicate);

			in.getValue().forEach(value -> verify(mockValueInExpression).value(value));
		}
	}
}
