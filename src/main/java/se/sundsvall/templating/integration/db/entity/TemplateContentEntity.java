package se.sundsvall.templating.integration.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "template_content")
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TemplateContentEntity {

	@Id
	@Column(name = "id")
	private String id;

	@OneToOne
	@JoinColumn(name = "id")
	@MapsId
	private TemplateEntity template;

	@Lob
	@Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
	private String content;
}
