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
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "templates_metadata")
public class Metadata {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(length = 32, nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    public Metadata() {
        id = UUID.randomUUID().toString();
    }
}
