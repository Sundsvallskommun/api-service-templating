package se.sundsvall.templating.api.domain.filter.specification.logic;

import java.util.function.BiFunction;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.logic.Not;
import se.sundsvall.templating.api.domain.filter.specification.ExpressionSpecification;

public class NotSpecification<T> extends ExpressionSpecification<T> {

	private static final long serialVersionUID = 2349969310918552422L;
	private final transient Not expression;
	private final transient BiFunction<Class<T>, Expression, Specification<T>> expressionMapper;

	public NotSpecification(final Class<T> entityClass, final Not expression) {
		this(entityClass, expression, FilterSpecifications::toSpecification);
	}

	public NotSpecification(final Class<T> entityClass, final Not expression,
		final BiFunction<Class<T>, Expression, Specification<T>> expressionMapper) {
		super(entityClass);

		this.expression = expression;
		this.expressionMapper = expressionMapper;
	}

	@Override
	public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
		final CriteriaBuilder criteriaBuilder) {
		return criteriaBuilder.not(
			expressionMapper.apply(getEntityClass(), expression.getExpression())
				.toPredicate(root, query, criteriaBuilder));
	}
}
