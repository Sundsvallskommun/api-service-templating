package se.sundsvall.templating.integration.db.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "templates_default_values")
public class DefaultValueEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, unique = true)
    private final String id = UUID.randomUUID().toString();

    @Column(name = "field_name", length = 32, nullable = false)
    private String fieldName;

    @Column(name = "`value`", nullable = false)
    private String value;
}