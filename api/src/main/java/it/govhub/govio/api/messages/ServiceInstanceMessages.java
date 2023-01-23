package it.govhub.govio.api.messages;

import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.messages.RestEntityMessageBuilder;

@Component
public class ServiceInstanceMessages extends RestEntityMessageBuilder{

	public ServiceInstanceMessages() { super("Service Instance"); 	}

}
