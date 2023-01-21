package it.govhub.govio.api.entity;

import java.nio.file.Path;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.utils.JpaPathConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "govio_files")
public class GovioFileEntity {

	public enum Status {CREATED, PROCESSING, PROCESSED}

	@Id @GeneratedValue
	private Long id;

	@ManyToOne
 	@JoinColumn(name = "id_govhub_user", nullable=false)
	private UserEntity govauthUser;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_service_instance", nullable = false, foreignKey = @ForeignKey(name = "GovioFile_GovioServiceInstance"))
	private GovioServiceInstanceEntity serviceInstance;
	
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "creation_date", nullable = false)
	private OffsetDateTime creationDate;
	
	@Column(name = "processing_date")
	private OffsetDateTime processingDate;
	
	@Convert(converter = JpaPathConverter.class)
	@Column(name = "location",  length = 1024, nullable = false)
	private Path location;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "status_detail",  length = 1024)
	private String statusDetail;
	
	@Column(name = "size")
	private long size;

	@Column(name = "acquired_messages")
	private Long acquiredMessages;

	@Column(name = "error_messages")
	private Long errorMessages;
	
	//TODO Da verificare!! Lorenzo vs Francesco
//	// L'entita collegata è readonly, non abbiamo cascade quindi possiamo salvare il GovioFileEntity senza problemi
//	@OneToMany(mappedBy = "govioFile")
//	@Builder.Default
//	private List<GovioFileMessageEntity> fileMessages = new ArrayList<>();
}
