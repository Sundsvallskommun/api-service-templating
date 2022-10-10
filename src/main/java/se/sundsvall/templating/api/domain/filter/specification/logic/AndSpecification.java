package se.sundsvall.templating.api.domain.filter.specification.logic;

import java.util.function.BiFunction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.logic.And;
import se.sundsvall.templating.api.domain.filter.specification.ExpressionSpecification;

public class AndSpecification<T> extends ExpressionSpecification<T> {

    private final And expression;
    private final BiFunction<Class<T>, Expression, Specification<T>> expressionMapper;

    public AndSpecification(final Class<T> entityClass, final And expression) {
        this(entityClass, expression, FilterSpecifications::toSpecification);
    }

    public AndSpecification(final Class<T> entityClass, final And expression,
            final BiFunction<Class<T>, Expression, Specification<T>> expressionMapper) {
        super(entityClass);

        this.expression = expression;
        this.expressionMapper = expressionMapper;
    }

    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
            final CriteriaBuilder criteriaBuilder) {
        var clausePredicates = expression.getExpressions().stream()
            .map(expression -> expressionMapper.apply(getEntityClass(), expression))
            .map(specification -> specification.toPredicate(root, query, criteriaBuilder))
            .toList();

        return criteriaBuilder
            .and(clausePredicates.toArray(new Predicate[0]));
    }
}
