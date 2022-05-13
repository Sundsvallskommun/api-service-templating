package se.sundsvall.templating.integration.db.entity;

import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
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
@Table(name = "templates")
public class TemplateEntity {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    private final String id = UUID.randomUUID().toString();

    @Column(name = "identifier", nullable = false, unique = true)
    private String identifier;

    @Column(name = "name", length = 64, nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "template_id", referencedColumnName = "id")
    private List<Metadata> metadata;

    @Column(name = "content", nullable = false)
    @Lob
    private String content;
}
