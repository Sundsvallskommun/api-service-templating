package se.sundsvall.templating.integration.db.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "templates_metadata")
@Getter
@Setter
@NoArgsConstructor
public class MetadataEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, unique = true)
    private final String id = UUID.randomUUID().toString();

    @Column(name = "metadata_key", length = 32, nullable = false)
    private String key;

    @Column(name = "`value`", nullable = false)
    private String value;

    /*
     * Custom @Builder-annotated constructor to exclude id from builder.
     */
    @Builder(setterPrefix = "with")
    MetadataEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
