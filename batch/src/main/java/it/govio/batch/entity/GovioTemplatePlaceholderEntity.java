package it.govio.batch.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
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
	@Id
	@SequenceGenerator(name="seq_govio_template_placeholders",sequenceName="seq_govio_template_placeholders", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_template_placeholders")
	private Long id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_placeholder", nullable = false)
	private GovioPlaceholderEntity govioPlaceholder;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_template", nullable = false)
	private GovioTemplateEntity govioTemplate;
}
