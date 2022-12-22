package it.govhub.govio.api.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.AuthorizationList;
import it.govhub.govregistry.commons.api.beans.AuthorizationOrdering;
import it.govhub.govregistry.commons.api.spec.AuthorizationApi;
import it.govhub.govregistry.commons.assemblers.AuthorizationAssembler;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity_;
import it.govhub.govregistry.commons.entity.RoleEntity_;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.commons.repository.RoleRepository;
import it.govhub.govregistry.commons.services.ReadRoleAuthorizationService;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;



@RestController
public class AuthorizationController implements AuthorizationApi {
	
	@Autowired
	AuthorizationAssembler authAssembler;
	
	@Autowired
	ReadRoleAuthorizationService authService;
	
	@Autowired
	RoleAuthorizationRepository authRepo;
	
	@Autowired
	RoleRepository roleRepo;
	
	@Autowired
	SecurityService securityService;

	@Override
	public ResponseEntity<AuthorizationList> listAuthorizations(Long userId, AuthorizationOrdering sort,  Direction sortDirection, Integer limit, Long offset) {
		
		this.securityService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_VIEWER);
		
		Sort orderBy;
		switch(sort) {
			case ID:
				orderBy = Sort.by(sortDirection, RoleAuthorizationEntity_.ID);
				break;
			case ROLE_NAME:
				orderBy = Sort.by(sortDirection, RoleAuthorizationEntity_.ROLE+"."+RoleEntity_.NAME);
				break;
			case UNSORTED:
				orderBy = Sort.unsorted();
				break;
			default:
				throw new UnreachableException();
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, orderBy);
		
		AuthorizationList ret = authService.listUserAuthorizations(userId, pageRequest);
		
		return ResponseEntity.ok(ret);
	}



}
