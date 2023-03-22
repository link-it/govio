package it.govhub.govio.api.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;

@V1RestController
public class OrganizationController  implements OrganizationApi {
	
	@Autowired
	ReadOrganizationController readOrganizationController;

	@Override
	public ResponseEntity<Resource> downloadOrganizationLogo(Long id) {
		return this.readOrganizationController.downloadOrganizationLogo(id);
	}


	@Override
	public ResponseEntity<Resource> downloadOrganizationLogoMiniature(Long id) {
		return this.readOrganizationController.downloadOrganizationLogoMiniature(id);
	}


	@Override
	public ResponseEntity<OrganizationList> listOrganizations(OrganizationOrdering sort, Direction sortDirection, Integer limit, Long offset, String q, List<String> withRoles) {
		return this.readOrganizationController.listOrganizations(sort, sortDirection, limit, offset, q, withRoles);
	}


	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		return this.readOrganizationController.readOrganization(id);
	}

}
