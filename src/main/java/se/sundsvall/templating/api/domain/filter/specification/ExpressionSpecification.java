package se.sundsvall.templating.api.domain.filter.specification;

import org.springframework.data.jpa.domain.Specification;

public abstract class ExpressionSpecification<T> implements Specification<T> {

    private final Class<T> entityClass;

    public ExpressionSpecification(final Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
}
