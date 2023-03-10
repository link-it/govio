package it.govhub.govio.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;

@Component
public class GovioConfig  implements ApplicationConfig {
	
	@Value("${application-id:govio")
	private String applicationId;

	@Override
	public String getApplicationId() {
		return this.applicationId;
	}

}