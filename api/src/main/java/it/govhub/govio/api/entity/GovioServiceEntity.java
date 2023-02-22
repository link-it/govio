package it.govhub.govio.api.entity;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import it.govhub.govregistry.commons.entity.ServiceEntity;
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
@Table(name = "govio_services")
public class GovioServiceEntity {
	
	@Id
	@SequenceGenerator(name="seq_govio_services",sequenceName="seq_govio_services", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_services")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_govio_template", nullable = false, foreignKey = @ForeignKey(name = "GovioService_GovioTemplate"))
	private GovioTemplateEntity govioTemplate;
	
	@OneToOne
	@JoinColumn(name = "id_govhub_service", nullable = false, foreignKey = @ForeignKey(name = "GovioService_GovhubService"))
	private ServiceEntity govhubService;
}
