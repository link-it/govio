package it.govhub.govio.api.services;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govio.api.security.GovioRoles;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.services.PermissionManager;
import it.govhub.security.services.SecurityService;

@Service
public class GovioPermissionManager implements PermissionManager {

	@Autowired
	SecurityService authService;
	
	@Autowired
	GovioServiceInstanceRepository serviceInstanceRepo;
	
	@Override
	public Set<Long> listReadableOrganizations(UserEntity user) {
		
		//	govio_sysadmin, govio_service_instance_editor, govio_service_instance_viewer non hanno vincoli particolari.
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(
				GovioRoles.GOVIO_SYSADMIN, 
				GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER,
				GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR);
		
		if (orgIds == null) {
			// Se già posso leggere tutto, non serve aggiungere altro.
			return null;
		}
		
		// govio_sender e govio_viewer possono solo leggere organizzazioni per le quali c'è una
		// serviceInstance associata, per cui prendo tali organizzazioni e le aggiungo a quelle
		// calcolate sopra
		
		if (this.authService.hasAnyRole(GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER)) {
			
			// Recupero tutte le organizzazioni per le quali c'è una service instance
			Set<Long> instanceOrgIds = this.serviceInstanceRepo.findAll().stream()
					.map(s -> s.getOrganization().getId())
					.collect(Collectors.toSet());
			
			// Le restringo su quelle per le quali ho autorizzazioni govio_sender e govio_viewer
			Set<Long> readableInstanceOrganizations = SecurityService.restrictAuthorizations(
					instanceOrgIds,
					this.authService.listAuthorizedOrganizations(GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER));
			
			// Aggiungo le organizzazioni trovate a quelle di sopra.
			orgIds.addAll(readableInstanceOrganizations);
		}

		return orgIds;
	}

	
	@Override
	public Set<Long> listReadableServices(UserEntity user) {

		//	govio_sysadmin, govio_service_instance_editor, govio_service_instance_viewer, govio_service_editor e govio_service_viewer non hanno vincoli particolari.
		Set<Long> serviceIds = this.authService.listAuthorizedServices(
				GovioRoles.GOVIO_SYSADMIN, 
				GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER,
				GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,
				GovioRoles.GOVIO_SERVICE_VIEWER,
				GovioRoles.GOVIO_SERVICE_EDITOR);
		
		if (serviceIds == null) {
			// Se già posso leggere tutto, non serve aggiungere altro.
			return null;
		}
		
		// govio_sender e govio_viewer possono solo leggere servizi per le quali c'è una
		// serviceInstance associata, per cui prendo tali servizi e li aggiungo a quelli
		// calcolati sopra
				
		if (this.authService.hasAnyRole(GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER)) {
			
			// Recupero tutte le organizzazioni per le quali c'è una service instance
			Set<Long> instanceServiceIds= this.serviceInstanceRepo.findAll().stream()
					.map(s -> s.getService().getGovhubService().getId())
					.collect(Collectors.toSet());
			
			// Le restringo su quelle per le quali ho autorizzazioni govio_sender e govio_viewer
			Set<Long> readableInstanceServices = SecurityService.restrictAuthorizations(
					instanceServiceIds,
					this.authService.listAuthorizedServices(GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER));
			
			// Aggiungo le organizzazioni trovate a quelle di sopra.
			serviceIds.addAll(readableInstanceServices);
		}
		
		return serviceIds;
	}

}
