package se.sundsvall.templating.api.domain.filter.specification.value;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EqSpecificationTests {

	static class Dummy {
	}

	@Mock
	private Root<Dummy> mockRoot;
	@Mock
	private Path<Object> mockPath;
	@Mock
	private CriteriaQuery<Dummy> mockQuery;
	@Mock
	private CriteriaBuilder mockCriteriaBuilder;
	@Mock
	private Predicate mockPredicate;

	@Test
	void constructorAndGetters() {
		var eq = new Eq("someKey", "someValue");

		var eqSpecification = new EqSpecification<Dummy>(eq);

		assertThat(eqSpecification.getExpression()).isEqualTo(eq);
	}

	@Test
	void toPredicate() {
		var eq = new Eq("someKey", "someValue");
		var eqSpecification = new EqSpecification<Dummy>(eq);

		when(mockRoot.get(eq.getKey())).thenReturn(mockPath);
		when(mockCriteriaBuilder.equal(mockPath, eq.getValue())).thenReturn(mockPredicate);

		var result = eqSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
		assertThat(result).isEqualTo(mockPredicate);

		verify(mockRoot).get(eq.getKey());
		verify(mockCriteriaBuilder).equal(mockPath, eq.getValue());
		verifyNoMoreInteractions(mockRoot, mockCriteriaBuilder);
		verifyNoInteractions(mockQuery, mockPath, mockPredicate);
	}
}
