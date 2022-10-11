package se.sundsvall.templating.api.domain.filter.specification.value;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.templating.api.domain.filter.expression.value.Eq;

public class EqSpecification<T> implements Specification<T> {

    private final Eq expression;

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
