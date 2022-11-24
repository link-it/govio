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
	@Table(name = "govio_service_instances")
	public class GovioServiceInstanceEntity {

	@Id
	@SequenceGenerator(name="seq_govio_service_instances",sequenceName="seq_govio_service_instances", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_service_instances")
	private Long id;

	@Column(name = "id_govio_service", nullable = false)
	private Long idGovioService;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_template", nullable = false)
	private GovioTemplateEntity govioTemplate;

	@Column(name = "apikey", nullable = false)
	private String apikey;
}
