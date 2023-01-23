package it.govio.template;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@Setter
public class Message extends BaseMessage {
		private String subject;
		private String markdown;
}
