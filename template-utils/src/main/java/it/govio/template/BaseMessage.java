package it.govio.template;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class BaseMessage {
		private String taxcode;
		private Long amount;
		private String noticeNumber;
		private Boolean invalidAfterDueDate;
		private String payee;
		private String email;
		private LocalDateTime scheduledExpeditionDate;
		private LocalDateTime dueDate;
}
