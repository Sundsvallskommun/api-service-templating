package se.sundsvall.templating.api.domain.filter.specification.logic;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.function.BiFunction;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.templating.api.domain.filter.FilterSpecifications;
import se.sundsvall.templating.api.domain.filter.expression.Expression;
import se.sundsvall.templating.api.domain.filter.expression.logic.Or;
import se.sundsvall.templating.api.domain.filter.specification.ExpressionSpecification;

public class OrSpecification<T> extends ExpressionSpecification<T> {

	private static final long serialVersionUID = 8156583042979332583L;
	private final transient Or expression;
	private final transient BiFunction<Class<T>, Expression, Specification<T>> expressionMapper;

	public OrSpecification(final Class<T> entityClass, final Or expression) {
		this(entityClass, expression, FilterSpecifications::toSpecification);
	}

	public OrSpecification(final Class<T> entityClass, final Or expression,
		final BiFunction<Class<T>, Expression, Specification<T>> expressionMapper) {
		super(entityClass);

		this.expression = expression;
		this.expressionMapper = expressionMapper;
	}

	@Override
	public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query,
		final CriteriaBuilder criteriaBuilder) {
		final var clausePredicates = expression.getExpressions().stream()
			.map(currentExpression -> expressionMapper.apply(getEntityClass(), currentExpression))
			.map(specification -> specification.toPredicate(root, query.distinct(true), criteriaBuilder))
			.toList();

		return criteriaBuilder
			.or(clausePredicates.toArray(new Predicate[0]));
	}
}
