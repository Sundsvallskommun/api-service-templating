package se.sundsvall.templating.integration.db.spec;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.templating.integration.db.entity.MetadataEntity_;
import se.sundsvall.templating.integration.db.entity.TemplateEntity;
import se.sundsvall.templating.integration.db.entity.TemplateEntity_;

public final class Specifications {

    private Specifications() { }

    public static Specification<TemplateEntity> hasMetadata(final String key, final String value) {
        return (root, query, cb) -> {
            var join = root.join(TemplateEntity_.metadata);

            return query.where(cb.isMember(join, root.get(TemplateEntity_.metadata)), cb.and(
                cb.equal(join.get(MetadataEntity_.key), key),
                cb.equal(join.get(MetadataEntity_.value), value)
            )).getRestriction();
        };
    }
}
