package se.sundsvall.templating.api.domain.filter.specification.value;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.templating.api.domain.filter.expression.value.In;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InSpecificationTests {

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
		var in = new In("someKey", List.of("someValue", "someOtherValue"));

		var inSpecification = new InSpecification<Dummy>(in);

		assertThat(inSpecification.getExpression()).isEqualTo(in);
	}

	@Test
	void toPredicate() {
		var values = List.of("someValue", "someOtherValue");
		var in = new In("someKey", values);
		var inSpecification = new InSpecification<Dummy>(in);

		when(mockRoot.get(in.getKey())).thenReturn(mockPath);
		when(mockPath.in(values)).thenReturn(mockPredicate);

		var result = inSpecification.toPredicate(mockRoot, mockQuery, mockCriteriaBuilder);
		assertThat(result).isEqualTo(mockPredicate);

		verify(mockRoot).get(in.getKey());
		verify(mockPath).in(values);
		verifyNoMoreInteractions(mockRoot, mockPath);
		verifyNoInteractions(mockQuery, mockCriteriaBuilder, mockPredicate);
	}
}
