package it.govio.batch.entity;


import java.time.LocalDateTime;

import javax.persistence.CascadeType;
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
import javax.persistence.OneToOne;
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
@Table(name = "govio_messages")
public class GovioMessageEntity {
	public enum Status {ACCEPTED, THROTTLED, SCHEDULED, RECIPIENT_ALLOWED, PROFILE_NOT_EXISTS, SENDER_NOT_ALLOWED, DENIED, SENT, BAD_REQUEST, FORBIDDEN, PROCESSED, CREATED}

	@Id
	@SequenceGenerator(name="seq_govio_messages",sequenceName="seq_govio_messages", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govio_messages")
	private Long id;
	
    @OneToOne(mappedBy = "govioMessage", cascade = {CascadeType.REMOVE,CascadeType.PERSIST})
//	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
//	@JoinColumn(name = "id_govio_file_messages", nullable = true)
	private GovioFileMessageEntity govioFileMessage;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_govio_service_instance", nullable = false)
	private GovioServiceInstanceEntity govioServiceInstance;
	
	@Column(name = "taxcode", nullable = false)
	private String taxcode;

	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "markdown", nullable = false)
	private String markdown;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;

	@Column(name = "amount")
	private Long amount;

	@Column(name = "notice_number")
	private String noticeNumber;

	@Column(name = "invalid_after_due_date")
	private Boolean invalidAfterDueDate;

	@Column(name = "payee")
	private String payee;

	@Column(name = "email")
	private String email;

	@Column(name = "appio_message_id")
	private String appioMessageId;

	@Column(name = "creation_date", nullable = false)
	private LocalDateTime creationDate;
	
	@Column(name = "scheduled_expedition_date", nullable = false)
	private LocalDateTime scheduledExpeditionDate;

	@Column(name = "expedition_date")
	private LocalDateTime expeditionDate;
	
	@Column(name = "due_date")
	private LocalDateTime dueDate;
	
	@Column(name = "last_update_status")
	private LocalDateTime lastUpdateStatus;

}
