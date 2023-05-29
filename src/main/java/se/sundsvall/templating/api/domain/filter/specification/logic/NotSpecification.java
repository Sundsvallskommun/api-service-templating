package se.sundsvall.templating.api.domain.filter.specification.logic;

import java.util.function.BiFunction;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.specification.ExpressionSpecification;

public class NotSpecification<T> extends ExpressionSpecification<T> {

    private final Not expression;
    private final BiFunction<Class<T>, Expression, Specification<T>> expressionMmapper;

    public NotSpecification(final Class<T> entityClass, final Not expression) {
        this(entityClass, expression, FilterSpecifications::toSpecification);
    }

    public NotSpecification(final Class<T> entityClass, final Not expression,
            final BiFunction<Class<T>, Expression, Specification<T>> expressionMmapper) {
        super(entityClass);

        this.expression = expression;
        this.expressionMmapper = expressionMmapper;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
            final CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.not(
            expressionMmapper.apply(getEntityClass(), expression.getExpression())
                .toPredicate(root, query, criteriaBuilder));
    }
}
