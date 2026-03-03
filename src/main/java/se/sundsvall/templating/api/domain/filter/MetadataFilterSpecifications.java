package se.sundsvall.templating.api.domain.filter;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.Serial;
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
import se.sundsvall.templating.integration.db.entity.MetadataEntity_;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity_;

public final class MetadataFilterSpecifications {

	private MetadataFilterSpecifications() {}

	public static Specification<TemplateEntity> toSpecification(final Expression expression) {
		return toSpecification(TemplateEntity.class, expression);
	}

	public static Specification<TemplateEntity> toSpecification(final Class<TemplateEntity> entityClass,
		final Expression expression) {
		return switch (expression) {
			case And andExpression -> new AndSpecification<>(TemplateEntity.class, andExpression,
				MetadataFilterSpecifications::toSpecification);
			case Or orExpression -> new OrSpecification<>(TemplateEntity.class, orExpression,
				MetadataFilterSpecifications::toSpecification);
			case Not notExpression -> new NotSpecification<>(TemplateEntity.class, notExpression);
			case Eq eqExpression -> new MetadataEqSpecification(eqExpression);
			case In inExpression -> new MetadataInSpecification(inExpression);
			case Empty _ -> FilterSpecifications.createEmptySpecification(entityClass);
			default -> throw new IllegalArgumentException("Unknown expression type: " + expression.getClass().getSimpleName());
		};
	}

	static class MetadataEqSpecification extends EqSpecification<TemplateEntity> {

		@Serial
		private static final long serialVersionUID = 1408454748967470019L;

		MetadataEqSpecification(final Eq expression) {
			super(expression);
		}

		@Override
		public Predicate toPredicate(final Root<TemplateEntity> root, final CriteriaQuery<?> query,
			final CriteriaBuilder criteriaBuilder) {
			final var join = root.join(TemplateEntity_.metadata, JoinType.LEFT);

			return query.where(
				criteriaBuilder.isMember(join, root.get(TemplateEntity_.metadata)),
				criteriaBuilder.and(
					criteriaBuilder.equal(join.get(MetadataEntity_.key), getExpression().getKey()),
					criteriaBuilder.equal(join.get(MetadataEntity_.value), getExpression().getValue()))).getRestriction();
		}
	}

	static class MetadataInSpecification extends InSpecification<TemplateEntity> {

		@Serial
		private static final long serialVersionUID = 7794045931616835220L;

		MetadataInSpecification(final In expression) {
			super(expression);
		}

		@Override
		public Predicate toPredicate(final Root<TemplateEntity> root, final CriteriaQuery<?> query,
			final CriteriaBuilder criteriaBuilder) {
			final var join = root.join(TemplateEntity_.metadata, JoinType.LEFT);

			final var inClause = criteriaBuilder.in(join.get(MetadataEntity_.value));
			getExpression().getValue().forEach(inClause::value);

			return query.where(
				criteriaBuilder.isMember(join, root.get(TemplateEntity_.metadata)),
				criteriaBuilder.and(
					criteriaBuilder.equal(join.get(MetadataEntity_.key), getExpression().getKey()),
					inClause)).getRestriction();
		}
	}
}
