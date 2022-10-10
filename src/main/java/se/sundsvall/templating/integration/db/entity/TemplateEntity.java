package se.sundsvall.templating.integration.db.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Entity
@Table(
    name = "templates",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_identifier_and_version",
        columnNames = { "identifier", "major", "minor" }
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private final String id = UUID.randomUUID().toString();

    @Column(name = "identifier", nullable = false)
    private String identifier;

    @Embedded
    @Setter(AccessLevel.PRIVATE)
    @With
    private Version version = Version.INITIAL_VERSION;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private List<MetadataEntity> metadata;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private Set<DefaultValueEntity> defaultValues;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "changelog")
    private String changeLog;

    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt = LocalDateTime.now();

    /*
     * Custom @Builder-annotated constructor to exclude id and version from builder.
     */
    @Builder(setterPrefix = "with")
    TemplateEntity(final String identifier, final String name, final String description,
            final String content, final String changeLog, final List<MetadataEntity> metadata,
            final Set<DefaultValueEntity> defaultValues) {
        this.identifier = identifier;
        this.name = name;
        this.description = description;
        this.content = content;
        this.changeLog = changeLog;
        this.metadata = metadata;
        this.defaultValues = defaultValues;
    }

    @PrePersist
    @PreUpdate
    void updateLastModifiedAt() {
        lastModifiedAt = LocalDateTime.now();
    }
}
