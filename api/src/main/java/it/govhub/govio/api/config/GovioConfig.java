package it.govhub.govio.api.config;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.config.ApplicationConfig;

@Component
public class GovioConfig  implements ApplicationConfig {
	
	@Value("${application-id:govio")
	private String applicationId;
	
	public static final Set<String> DEFAULT_READ_ORGANIZATION_ROLES = Set.of(

			GovioRoles.GOVIO_SYSADMIN, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,
			GovioRoles.GOVIO_VIEWER, 
			GovioRoles.GOVIO_SENDER);
	
	public static final Set<String> DEFAULT_READ_SERVICE_ROLES = Set.of(
			GovioRoles.GOVIO_SYSADMIN, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER,
			GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR	);

	@Override
	public String getApplicationId() {
		return this.applicationId;
	}

	@Override
	public Set<String> getReadServiceRoles() {
		return DEFAULT_READ_ORGANIZATION_ROLES;
	}

	@Override
	public Set<String> getReadOrganizationRoles() {
		return DEFAULT_READ_SERVICE_ROLES;
	}

}