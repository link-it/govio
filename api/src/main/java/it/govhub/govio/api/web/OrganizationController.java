package it.govhub.govio.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.assemblers.OrganizationAssembler;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.repository.OrganizationRepository;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@RestController
public class OrganizationController implements OrganizationApi{
	
	@Autowired
	private OrganizationAssembler orgAssembler;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private SecurityService authService;

	
	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_VIEWER, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR);

		Organization ret = this.orgRepo.findById(id)
			.map( org -> this.orgAssembler.toModel(org))
			.orElseThrow( () -> new ResourceNotFoundException("Organization with id  ["+id+"] not found."));
		
		return ResponseEntity.ok(ret);
	}


}
