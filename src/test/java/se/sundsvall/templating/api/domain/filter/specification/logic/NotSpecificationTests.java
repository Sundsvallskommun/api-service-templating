package se.sundsvall.templating.api.domain.filter.specification.logic;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotSpecificationTests {

	static class Dummy {
	}

	@Mock
	private Root<Dummy> mockRoot;
	@Mock
	private CriteriaQuery<Dummy> mockQuery;
	@Mock
	private CriteriaBuilder mockCriteriaBuilder;
	@Mock
	private Specification<Dummy> mockSpecification;
	@Mock
	private Predicate initialMockPredicate;
	@Mock
	private Predicate mockPredicate;

	@Test
	void constructorAndGetters() {
		var not = new Not(new Eq("someKey", "someValue"));
		var notSpecification = new NotSpecification<>(Dummy.class, not);

		assertThat(notSpecification.getEntityClass()).isEqualTo(Dummy.class);
	}

	@Test
	void toPredicate() {
		var not = new Not(new Eq("someKey", "someValue"));
		var notSpecification = new NotSpecification<>(Dummy.class, not);

		try (var filterSpecificationsStaticMock = mockStatic(FilterSpecifications.class)) {
			filterSpecificationsStaticMock.when(() -> FilterSpecifications.toSpecification(Dummy.class, not.expression()))
				.thenReturn(mockSpecification);

			when(mockSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder)).thenReturn(initialMockPredicate);
			when(mockCriteriaBuilder.not(initialMockPredicate)).thenReturn(mockPredicate);

			var result = notSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			assertThat(result).isEqualTo(mockPredicate);

			verify(mockSpecification).toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
			verify(mockCriteriaBuilder).not(initialMockPredicate);
			verifyNoMoreInteractions(mockSpecification, mockCriteriaBuilder);
			verifyNoInteractions(mockRoot, mockQuery, initialMockPredicate, mockPredicate);

			filterSpecificationsStaticMock.verify(() -> FilterSpecifications.toSpecification(Dummy.class, not.expression()));
			filterSpecificationsStaticMock.verifyNoMoreInteractions();
		}
	}
}
