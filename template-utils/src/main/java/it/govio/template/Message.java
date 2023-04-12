package it.govio.template;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Getter
@Setter
@Jacksonized
public class Message extends BaseMessage {
		private String subject;
		private String markdown;
}
