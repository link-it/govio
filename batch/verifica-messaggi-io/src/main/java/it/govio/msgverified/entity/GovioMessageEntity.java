package it.govio.msgverified.entity;

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
		public enum Status {ACCEPTED, THROTTLED, SENT, DENIED, FORBIDDEN, NOT_FOUND}

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

		@Column(name = "status", nullable = false)
		@Enumerated(EnumType.STRING)
		private Status status;
		
		@Column(name = "expedition_date")
		private LocalDateTime expeditionDate;

		@Column(name = "last_update_status")
		private LocalDateTime lastUpdateStatus;
		
		@Column(name = "appio_message_id")
		private String appio_message_id;
	}
