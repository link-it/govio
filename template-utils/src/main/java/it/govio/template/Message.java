package it.govio.template;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Message {
		private String taxcode;
		private String subject;
		private String markdown;
		private Long amount;
		private String noticeNumber;
		private Boolean invalidAfterDueDate;
		private String payee;
		private String email;
		private LocalDateTime scheduledExpeditionDate;
		private LocalDateTime dueDate;
}
