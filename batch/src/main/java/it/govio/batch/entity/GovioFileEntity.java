package it.govio.batch.entity;


import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

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
@Table(name = "govio_files")
@Component("govioFileEntity")
public class GovioFileEntity {

	public enum Status {CREATED, PROCESSING, PROCESSED}

	@Id
	@SequenceGenerator(name="seq_govio_files",sequenceName="seq_govio_files", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_files")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_service_instance", nullable = false)
	private GovioServiceInstanceEntity govioServiceInstance;

	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "location", nullable = false)
	private String location;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "status_detail")
	private String statusDetail;
	
	@Column(name = "message_body")
	private String messageBody;

	@Column(name = "acquired_messages")
	private Long acquiredMessages;

	@Column(name = "error_messages")
	private Long errorMessages;
	
	@Column(name = "creation_date", nullable = false)
	private LocalDateTime creationDate;
	
}
