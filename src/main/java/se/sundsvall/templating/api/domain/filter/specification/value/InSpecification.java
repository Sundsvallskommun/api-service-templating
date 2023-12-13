package se.sundsvall.templating.api.domain.filter.specification.value;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.sundsvall.templating.api.domain.filter.expression.value.In;

public class InSpecification<T> implements Specification<T> {

	private static final long serialVersionUID = -2759064320535918913L;
	private final transient In expression;

	public InSpecification(final In expression) {
		this.expression = expression;
	}

	public In getExpression() {
		return expression;
	}

	@Override
	public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder criteriaBuilder) {
		return root.get(expression.getKey()).in(expression.getValue());
	}
}
