package it.govhub.govio.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.spec.ServiceApi;
import it.govhub.govregistry.commons.assemblers.ServiceAssembler;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.repository.ServiceRepository;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@RestController
public class ServiceController implements ServiceApi {
	
	@Autowired
	private ServiceAssembler serviceAssembler;
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private SecurityService authService;
	
	@Override
	public ResponseEntity<Service> readService(Long id) {
		
		this.authService.hasAnyServiceAuthority(id, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_VIEWER, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR);
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Service with ID ["+id+"] not found."));
	
		return ResponseEntity.ok(this.serviceAssembler.toModel(service));
	}

}
