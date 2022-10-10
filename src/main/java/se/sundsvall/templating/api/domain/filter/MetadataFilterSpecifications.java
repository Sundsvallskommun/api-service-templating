package se.sundsvall.templating.api.domain.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
import se.sundsvall.templating.integration.db.entity.MetadataEntity_;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity_;

public final class MetadataFilterSpecifications {

    private MetadataFilterSpecifications() { }

    public static Specification<TemplateEntity> toSpecification(final Expression expression) {
        return toSpecification(TemplateEntity.class, expression);
    }

    public static Specification<TemplateEntity> toSpecification(final Class<TemplateEntity> entityClass,
            final Expression expression) {
        if (expression instanceof And andExpression) {
            return new AndSpecification<>(TemplateEntity.class, andExpression,
                MetadataFilterSpecifications::toSpecification);
        } else if (expression instanceof Or orExpression) {
            return new OrSpecification<>(TemplateEntity.class, orExpression,
                MetadataFilterSpecifications::toSpecification);
        } else if (expression instanceof Not notExpression) {
            return new NotSpecification<>(TemplateEntity.class, notExpression);
        } else if (expression instanceof Eq eqExpression) {
            return new MetadataEqSpecification(eqExpression);
        } else if (expression instanceof In inExpression) {
            return new MetadataInSpecification(inExpression);
        } else if (expression instanceof EmptyExpression) {
            return FilterSpecifications.createEmptySpecification(entityClass);
        }

        throw new IllegalArgumentException("Unknown expression type: " + expression.getClass().getSimpleName());
    }

    static class MetadataEqSpecification extends EqSpecification<TemplateEntity> {

        MetadataEqSpecification(final Eq expression) {
            super(expression);
        }

        @Override
        public Predicate toPredicate(final Root<TemplateEntity> root, final CriteriaQuery<?> query,
                final CriteriaBuilder criteriaBuilder) {
            var join = root.join(TemplateEntity_.metadata, JoinType.LEFT);

            return query.where(
                criteriaBuilder.isMember(join, root.get(TemplateEntity_.metadata)),
                criteriaBuilder.and(
                    criteriaBuilder.equal(join.get(MetadataEntity_.key), getExpression().getKey()),
                    criteriaBuilder.equal(join.get(MetadataEntity_.value), getExpression().getValue())
            )).getRestriction();
        }
    }

    static class MetadataInSpecification extends InSpecification<TemplateEntity> {

        MetadataInSpecification(final In expression) {
            super(expression);
        }

        @Override
        public Predicate toPredicate(final Root<TemplateEntity> root, final CriteriaQuery<?> query,
                final CriteriaBuilder criteriaBuilder) {
            var join = root.join(TemplateEntity_.metadata, JoinType.LEFT);

            var inClause = criteriaBuilder.in(join.get(MetadataEntity_.value));
            getExpression().getValue().stream().forEach(inClause::value);

            return query.where(
                criteriaBuilder.isMember(join, root.get(TemplateEntity_.metadata)),
                criteriaBuilder.and(
                    criteriaBuilder.equal(join.get(MetadataEntity_.key), getExpression().getKey()),
                    inClause
            )).getRestriction();
        }
    }
}
