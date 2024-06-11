package se.sundsvall.templating.integration.db.entity;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import se.sundsvall.templating.domain.TemplateType;

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
    private Version version = new Version(1, 0);

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 16, nullable = false)
    private TemplateType type;

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
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "changelog")
    private String changeLog;

    @Column(name = "last_modified_at", nullable = false)
    private LocalDateTime lastModifiedAt = LocalDateTime.now();

    /*
     * Custom @Builder-annotated constructor to exclude id and version from builder.
     */
    @Builder(setterPrefix = "with")
    TemplateEntity(final String identifier, final TemplateType type, final String name,
            final String description, final String content, final String changeLog,
            final List<MetadataEntity> metadata, final Set<DefaultValueEntity> defaultValues) {
        this.identifier = identifier;
        this.type = type;
        this.name = name;
        this.description = description;
        this.content = content;
        this.changeLog = changeLog;
        this.metadata = metadata;
        this.defaultValues = defaultValues;
    }

    @Transient
    public byte[] getContentBytes() {
        return ofNullable(content).map(content -> content.getBytes(UTF_8)).orElse(null);
    }

    @PrePersist
    @PreUpdate
    void updateLastModifiedAt() {
        lastModifiedAt = LocalDateTime.now();
    }
}
