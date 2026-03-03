package se.sundsvall.templating.api.domain.filter;

import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.templating.api.domain.filter.expression.Empty;
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

	private FilterSpecifications() {}

	public static <T> Specification<T> toSpecification(final Class<T> entityClass, final Expression expression) {
		return switch (expression) {
			case And andExpression -> new AndSpecification<>(entityClass, andExpression);
			case Or orExpression -> new OrSpecification<>(entityClass, orExpression);
			case Not notExpression -> new NotSpecification<>(entityClass, notExpression);
			case Eq eqExpression -> new EqSpecification<>(eqExpression);
			case In inExpression -> new InSpecification<>(inExpression);
			case Empty _ -> createEmptySpecification(entityClass);
			default -> throw new IllegalArgumentException("Unknown expression type: " + expression.getClass().getSimpleName());
		};
	}

	public static <T> Specification<T> createEmptySpecification(final Class<T> entityClass) {
		return (root, query, criteriaBuilder) -> criteriaBuilder.createQuery(entityClass).getRestriction();
	}
}
