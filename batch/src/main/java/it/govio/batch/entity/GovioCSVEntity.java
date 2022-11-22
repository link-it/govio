package it.govio.batch.entity;

import java.time.LocalDateTime;

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
/*
@Setter
@Getter	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "govio_CSV")

public class GovioCSVEntity {
	@Id
	@SequenceGenerator(name="seq_govio_messages",sequenceName="seq_govio_messages", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_messages")
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_service_instance", nullable = false)
	private GovioServiceInstanceEntity govioServiceInstance;

	@Column(name = "taxcode", nullable = false)
	private String taxcode;

	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "markdown", nullable = false)
	private String markdown;

	@Column(name = "amount")
	private Integer amount;

	@Column(name = "payee_taxcode")
	private String payee_taxcode;
	
	@Column(name = "notice_number")
	private String notice_number;

	@Column(name = "expedition_date")
	private LocalDateTime expeditionDate;
	
	@Column(name = "due_date")
	private LocalDateTime dueDate;
	
	@Column(name = "invalid_after_due_date")
	private Boolean invalidAfterDueDate;
}
*/
