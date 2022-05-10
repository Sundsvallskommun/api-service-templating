package se.sundsvall.templating.integration.db.entity;

import org.springframework.data.jpa.domain.Specification;

public final class Specifications {

    private Specifications() { }

    public static Specification<TemplateEntity> hasMetadata(final String key, final String value) {
        return (root, query, cb) -> {
            var join = root.join(TemplateEntity_.metadata);

            return query.where(cb.isMember(join, root.get(TemplateEntity_.metadata)), cb.and(
                cb.equal(join.get(Metadata_.key), key),
                cb.equal(join.get(Metadata_.value), value)
            )).getRestriction();
        };
    }
}
