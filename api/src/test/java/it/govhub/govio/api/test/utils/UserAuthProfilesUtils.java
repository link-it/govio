package it.govhub.govio.api.test.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import it.govhub.security.services.GovhubUserDetailService;

@Component
public class UserAuthProfilesUtils {
	
	@Autowired
	private GovhubUserDetailService userDetailService;

	@Transactional
	public RequestPostProcessor utenzaAdmin() {
		return utenzaPrincipal("amministratore");
	}

	public RequestPostProcessor utenzaUserViewer() {
	    return utenzaPrincipal("user_viewer"); 
	}

	public RequestPostProcessor utenzaUserEditor() {
	    return utenzaPrincipal("user_editor"); 
	}
	
	public RequestPostProcessor utenzaOrganizationViewer() {
	    return utenzaPrincipal("org_viewer"); 
	}

	public RequestPostProcessor utenzaOrganizationEditor() {
	    return utenzaPrincipal("org_editor"); 
	}
	
	public RequestPostProcessor utenzaServiceViewer() {
	    return utenzaPrincipal("service_viewer"); 
	}

	public RequestPostProcessor utenzaServiceEditor() {
	    return utenzaPrincipal("service_editor"); 
	}

	public RequestPostProcessor utenzaOspite() {
	    return utenzaPrincipal("ospite"); 
	}

	public RequestPostProcessor utenzaPrincipal(String principal) {
		return user(this.userDetailService.loadUserByUsername(principal));
	}

}
