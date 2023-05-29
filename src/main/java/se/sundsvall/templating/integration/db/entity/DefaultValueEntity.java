package se.sundsvall.templating.integration.db.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "templates_default_values")
@Getter
@Setter
@NoArgsConstructor
public class DefaultValueEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, unique = true)
    private final String id = UUID.randomUUID().toString();

    @Column(name = "field_name", length = 32, nullable = false)
    private String fieldName;

    @Column(name = "`value`", nullable = false)
    private String value;

    /*
     * Custom @Builder-annotated constructor to exclude id from builder.
     */
    @Builder(setterPrefix = "with")
    DefaultValueEntity(final String fieldName, final String value) {
        this.fieldName = fieldName;
        this.value = value;
    }
}
