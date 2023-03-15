package it.govhub.govio.api.messages;

import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.messages.RestEntityMessageBuilder;

@Component
public class TemplateMessages extends RestEntityMessageBuilder{

	public TemplateMessages() { super("Template"); }

}
