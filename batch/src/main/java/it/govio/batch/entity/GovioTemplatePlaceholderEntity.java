package it.govio.batch.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_placeholder", nullable = false)
	private GovioPlaceholderEntity govioPlaceholder;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_template", nullable = false)
	private GovioTemplateEntity govioTemplate;
	
	@Column(name = "index", nullable = false)
	private int index;
	
	@Column(name = "mandatory", nullable = false)
	private boolean mandatory;
}
