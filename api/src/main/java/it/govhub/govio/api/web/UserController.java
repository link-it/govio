package it.govhub.govio.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.spec.UserApi;
import it.govhub.govregistry.commons.assemblers.UserAssembler;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.repository.UserRepository;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;



@RestController
public class UserController implements UserApi {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserAssembler userAssembler;
	
	@Autowired
	private SecurityService authService;
	
	
	@Override
	public ResponseEntity<User> readUser(Long id) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_VIEWER);
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("User with id  ["+id+"] not found"));
		
		return ResponseEntity.ok(
				this.userAssembler.toModel(user));
	}

}
