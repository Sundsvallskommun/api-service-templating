package se.sundsvall.templating.api.domain.filter.specification.value;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.templating.api.domain.filter.expression.value.Eq;

public class EqSpecification<T> implements Specification<T> {

    private final transient Eq expression;

    public EqSpecification(final Eq expression) {
        this.expression = expression;
    }

    public Eq getExpression() {
        return expression;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.equal(root.get(expression.getKey()), expression.getValue());
    }
}
