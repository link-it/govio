package it.govio.batch.entity;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "govio_template_placeholders")
public class GovioTemplatePlaceholderEntity {

	@EmbeddedId
	private GovioTemplatePlaceholderKey id;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@MapsId("GovioTemplateId")
	@JoinColumn(name = "id_govio_template", nullable = false)
	private GovioTemplateEntity govioTemplate;

	@ManyToOne(fetch = FetchType.EAGER)
	@MapsId("GovioPlaceholderId")
	@JoinColumn(name = "id_govio_placeholder", nullable = false)
	private GovioPlaceholderEntity govioPlaceholder;

	@Column(name = "index", nullable = false)
	private int index;

	@Column(name = "mandatory", nullable = false)
	private boolean mandatory;

}

@Setter
@Getter	
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
class GovioTemplatePlaceholderKey implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "id_govio_placeholder")
	private Long govioPlaceholder;

	@Column(name = "id_govio_template")
	private Long govioTemplate;
}



