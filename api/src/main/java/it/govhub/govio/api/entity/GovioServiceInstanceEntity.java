package it.govhub.govio.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
		name = "govio_service_instances", 
		uniqueConstraints = { @UniqueConstraint( columnNames = { "id_govio_service", "id_govhub_organization" }) }
	)
public class GovioServiceInstanceEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@SequenceGenerator(name = "seq_govio_service_instances", sequenceName = "seq_govio_service_instances", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govio_service_instances")
	private Long id;

	@EqualsAndHashCode.Include
	@ManyToOne
	@JoinColumn(name = "id_govio_service", nullable = false, foreignKey = @ForeignKey(name = "GovioServiceInstance_GovioService"))
	private GovioServiceEntity service;

	@ManyToOne
	@JoinColumn(name = "id_govio_template", nullable = true, foreignKey = @ForeignKey(name = "GovioServiceInstance_GovioTemplate"))
	private GovioTemplateEntity template;
	
	@EqualsAndHashCode.Include
	@ManyToOne
	@JoinColumn(name = "id_govhub_organization", nullable = false, foreignKey = @ForeignKey(name = "GovioServiceInstance_GovhubOrganization"))
	private OrganizationEntity organization;

	@Column(name = "apikey", nullable = false)
	private String apikey;
}
