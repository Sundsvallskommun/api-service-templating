package se.sundsvall.templating.api.domain.filter.specification.logic;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;
import se.sundsvall.templating.api.domain.filter.expression.value.In;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrSpecificationTests {

	static class Dummy {
	}

	@Mock
	private Root<Dummy> mockRoot;
	@Mock
	private CriteriaQuery<Dummy> mockQuery;
	@Mock
	private CriteriaBuilder mockCriteriaBuilder;
	@Mock
	private Specification<Dummy> mockSpecification1;
	@Mock
	private Specification<Dummy> mockSpecification2;
	@Mock
	private Predicate initialMockPredicate1;
	@Mock
	private Predicate initialMockPredicate2;
	@Mock
	private Predicate mockPredicate;

	@Test
	void constructorAndGetters() {
		var eq = new Eq("someKey", "someValue");
		var in = new In("someOtherKey", List.of("someSecondValue", "someThirdValue"));
		var or = new Or(List.of(eq, in));
		var orSpecification = new OrSpecification<>(Dummy.class, or);

		assertThat(orSpecification.getEntityClass()).isEqualTo(Dummy.class);
	}

	@Test
	void toPredicate() {
		var eq = new Eq("someKey", "someValue");
		var in = new In("someOtherKey", List.of("someSecondValue", "someThirdValue"));
		var or = new Or(List.of(eq, in));
		var orSpecification = new OrSpecification<>(Dummy.class, or);

		try (var filterSpecificationsStaticMock = mockStatic(FilterSpecifications.class)) {
			filterSpecificationsStaticMock.when(() -> FilterSpecifications.toSpecification(Dummy.class, eq))
				.thenReturn(mockSpecification1);
			filterSpecificationsStaticMock.when(() -> FilterSpecifications.toSpecification(Dummy.class, in))
				.thenReturn(mockSpecification2);

			when(mockSpecification1.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder))
				.thenReturn(initialMockPredicate1);
			when(mockSpecification2.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder))
				.thenReturn(initialMockPredicate2);
			when(mockCriteriaBuilder.or(List.of(initialMockPredicate1, initialMockPredicate2))).thenReturn(mockPredicate);
			when(mockQuery.distinct(true)).thenReturn(mockQuery);

			var result = orSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			assertThat(result).isEqualTo(mockPredicate);

			verify(mockSpecification1).toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			verify(mockSpecification2).toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			verify(mockCriteriaBuilder).or(List.of(initialMockPredicate1, initialMockPredicate2));
			verify(mockQuery, times(2)).distinct(true);
			verifyNoMoreInteractions(mockSpecification1, mockSpecification2, mockCriteriaBuilder, mockQuery);
			verifyNoInteractions(mockRoot);

			filterSpecificationsStaticMock.verify(() -> FilterSpecifications.toSpecification(Dummy.class, eq));
			filterSpecificationsStaticMock.verify(() -> FilterSpecifications.toSpecification(Dummy.class, in));
			filterSpecificationsStaticMock.verifyNoMoreInteractions();
		}
	}
}
