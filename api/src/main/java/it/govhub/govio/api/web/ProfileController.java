package it.govhub.govio.api.web;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.api.spec.ProfileApi;
import it.govhub.govregistry.commons.assemblers.ProfileAssembler;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.config.GovregistryRoles;


@RestController
public class ProfileController implements ProfileApi {
	
	@Autowired
	ProfileAssembler profileAssembler;

	@Override
	public ResponseEntity<Profile> profile() {
	
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		GovhubPrincipal principal = (GovhubPrincipal) authentication.getPrincipal();
		
		Profile ret = this.profileAssembler.toModel(principal.getUser());
		
		// Rimuovo dal body tutte le autorizzazioni che non riguardano i ruoli di GovRegistry
		ret.setAuthorizations(
				ret.getAuthorizations().stream()
					.filter( auth -> GovregistryRoles.ruoliConsentiti.contains(auth.getRole().getRoleName()))
					.collect(Collectors.toList()));
		
		return ResponseEntity.ok(ret);
	}

}
