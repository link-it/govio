package it.govhub.govio.api.web;

import java.util.HashSet;
import java.util.Set;

import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.web.ReadServiceController;

@V1RestController
public class ServiceController extends ReadServiceController {

	private static Set<String> readServiceRoles = Set.of(
			GovioRoles.GOVIO_SYSADMIN, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER,
			GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,
			GovioRoles.GOVIO_SERVICE_VIEWER,
			GovioRoles.GOVIO_SERVICE_EDITOR);

	
	@Override
	protected Set<String> getReadServiceRoles() {
		return new HashSet<>(readServiceRoles);
	}

}
