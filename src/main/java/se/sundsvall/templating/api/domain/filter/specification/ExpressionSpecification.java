package se.sundsvall.templating.api.domain.filter.specification;

import java.io.Serial;
import org.springframework.data.jpa.domain.Specification;

public abstract class ExpressionSpecification<T> implements Specification<T> {

	@Serial
	private static final long serialVersionUID = -7983703150912293784L;

	private final Class<T> entityClass;

	protected ExpressionSpecification(final Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}
}
