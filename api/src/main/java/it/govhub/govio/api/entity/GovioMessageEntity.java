package it.govhub.govio.api.entity;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.entity.UserEntity;
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
@Component
@Table(name = "govio_messages")
public class GovioMessageEntity {
	public enum Status {ACCEPTED, THROTTLED, SCHEDULED, RECIPIENT_ALLOWED, PROFILE_NOT_EXISTS, SENDER_NOT_ALLOWED, DENIED, SENT, BAD_REQUEST, FORBIDDEN, PROCESSED, CREATED}

	@Id
	@SequenceGenerator(name = "seq_govio_messages", sequenceName = "seq_govio_messages", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govio_messages")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "id_govio_service_instance", nullable = false, foreignKey = @ForeignKey(name = "GovioMessage_GovioServiceInstance"))
	private GovioServiceInstanceEntity govioServiceInstance;
	
	@ManyToOne
	@JoinColumn(name = "id_govhub_user", nullable = false)
	private UserEntity sender; 

	@Column(name = "taxcode", nullable = false)
	private String taxcode;

	@Column(name = "subject", nullable = false)
	private String subject;

	@Column(name = "markdown", nullable = false, columnDefinition = "TEXT")
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
	private String payeeTaxcode;

	@Column(name = "email")
	private String email;

	@Column(name = "appio_message_id")
	private String appioMessageId;

	@Column(name = "creation_date", nullable = false)
	private OffsetDateTime creationDate;

	@Column(name = "scheduled_expedition_date", nullable = false)
	private OffsetDateTime scheduledExpeditionDate;

	@Column(name = "expedition_date")
	private OffsetDateTime expeditionDate;

	@Column(name = "due_date")
	private OffsetDateTime dueDate;

	@Column(name = "last_update_status")
	private OffsetDateTime lastUpdateStatus;

}
