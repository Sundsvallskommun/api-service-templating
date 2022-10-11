package se.sundsvall.templating.api.domain.filter;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.templating.api.domain.filter.expression.EmptyExpression;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.logic.And;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.expression.value.Eq;
import se.sundsvall.templating.api.domain.filter.expression.value.In;
import se.sundsvall.templating.api.domain.filter.specification.logic.AndSpecification;
import se.sundsvall.templating.api.domain.filter.specification.logic.NotSpecification;
import se.sundsvall.templating.api.domain.filter.specification.logic.OrSpecification;
import se.sundsvall.templating.api.domain.filter.specification.value.EqSpecification;
import se.sundsvall.templating.api.domain.filter.specification.value.InSpecification;

public final class FilterSpecifications {

    private FilterSpecifications() { }

    public static <T> Specification<T> toSpecification(final Class<T> entityClass, final Expression expression) {
        if (expression instanceof And andExpression) {
            return new AndSpecification<>(entityClass, andExpression);
        } else if (expression instanceof Or orExpression) {
            return new OrSpecification<>(entityClass, orExpression);
        } else if (expression instanceof Not notExpression) {
            return new NotSpecification<>(entityClass, notExpression);
        } else if (expression instanceof Eq eqExpression) {
            return new EqSpecification<>(eqExpression);
        } else if (expression instanceof In inExpression) {
            return new InSpecification<>(inExpression);
        } else if (expression instanceof EmptyExpression) {
            return createEmptySpecification(entityClass);
        }

        throw new IllegalArgumentException("Unknown expression type: " + expression.getClass().getSimpleName());
    }

    public static <T> Specification<T> createEmptySpecification(final Class<T> entityClass) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.createQuery(entityClass).getRestriction();
    }
}
