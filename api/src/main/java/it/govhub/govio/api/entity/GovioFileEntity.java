package it.govhub.govio.api.entity;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import java.util.ArrayList;

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
public class GovioFileEntity  {
	
	public enum Status {CREATED, PROCESSING, PROCESSED}

	@Id
	@SequenceGenerator(name = "seq_govio_files", sequenceName = "seq_govio_files", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govio_files")
	private Long id;

	@ManyToOne
    @JoinColumn(name = "id_govauth_user", nullable=false)
	private UserEntity govauthUser;
	
	@ManyToOne
    @JoinColumn(name = "id_govio_service_instance", nullable=false)
	private ServiceInstanceEntity serviceInstance;
	
	@Column(name = "name", nullable = false, length = 256)
	private String name;
	
	@Column(name = "expiration_date", nullable = false)
	private OffsetDateTime creationDate;
	
	@Column(name = "processing_date")
	private OffsetDateTime processingDate;
	
	@Column(name = "status",  length = 256, nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "status_detail",  length = 1024)
	private String statusDetail;
	
	@Convert(converter = JpaPathConverter.class)
	@Column(name = "location",  length = 1024, nullable = false)
	private Path location;
	
	@Column(name = "size")
	private long size;
	
	// L'entita collegata è readonly, non abbiamo cascade quindi possiamo salvare il GovioFileEntity senza problemi
	@OneToMany(mappedBy = "govioFile")
	@Builder.Default
	private List<GovioFileMessageEntity> fileMessages = new ArrayList<>();
}
