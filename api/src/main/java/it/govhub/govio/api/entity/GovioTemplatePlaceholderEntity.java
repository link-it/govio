package it.govhub.govio.api.entity;


import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "govio_template_placeholders")
public class GovioTemplatePlaceholderEntity {
	
	@EqualsAndHashCode.Include
	@EmbeddedId
	private GovioTemplatePlaceholderKey id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@MapsId("GovioTemplateId")
	@JoinColumn(name = "id_govio_template", nullable = false, foreignKey = @ForeignKey(name = "GovioTemplatePlaceholder_GovioTemplate"))
	private GovioTemplateEntity govioTemplate;

	@ManyToOne()
	@MapsId("GovioPlaceholderId")
	@JoinColumn(name = "id_govio_placeholder", nullable = false, foreignKey = @ForeignKey(name = "GovioTemplatePlaceholder_GovioPlaceholder"))
	private GovioPlaceholderEntity govioPlaceholder;

	@Column(name = "position", nullable = false)
	private int position;

	@Column(name = "mandatory", nullable = false)
	private boolean mandatory;

}




