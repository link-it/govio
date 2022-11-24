package it.govio.batch.entity;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

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
	
	@ManyToOne(fetch = FetchType.EAGER)
    @MapsId("GovioPlaceholderId")
	@JoinColumn(name = "id_govio_placeholder", nullable = false)
	private GovioPlaceholderEntity govioPlaceholder;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @MapsId("GovioTemplateId")
	@JoinColumn(name = "id_govio_template", nullable = false)
	private GovioTemplateEntity govioTemplate;

	@Column(name = "index")
	private int index;
	
	@Column(name = "mandatory")
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

