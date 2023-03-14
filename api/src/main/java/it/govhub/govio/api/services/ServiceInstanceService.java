package it.govhub.govio.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.FileFilters;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govregistry.commons.exception.SemanticValidationException;

@Service
public class ServiceInstanceService {
	
	Logger log = LoggerFactory.getLogger(ServiceInstanceService.class);
	
	@Autowired
	ServiceInstanceRepository instanceRepo;
	
	@Autowired
	ServiceInstanceMessages instanceMessages;
	
	@Autowired
	FileRepository fileRepo;
	
	public GovioServiceInstanceEntity replaceInstance(GovioServiceInstanceEntity oldInstance, GovioServiceInstanceEntity newInstance) {
		if (!oldInstance.getId().equals(newInstance.getId()) ) {
			throw new SemanticValidationException(this.instanceMessages.fieldNotModificable("id"));
		}
		
		if(!oldInstance.getService().getId().equals(newInstance.getService().getId()) || 
			!oldInstance.getOrganization().getId().equals(newInstance.getOrganization().getId()) ||
			!oldInstance.getTemplate().getId().equals(newInstance.getTemplate().getId())) {
				throw new SemanticValidationException("Non è possibile modificare i riferimenti al template, organizzazione o servizio della ServiceInstance");
		}
		
		return this.instanceRepo.save(newInstance);
	}
	

	public GovioServiceInstanceEntity disableIntance(GovioServiceInstanceEntity instance) {
		 var spec = FileFilters.byMessageStatus(FileService.IntermediateStatuses).and(FileFilters.byServiceInstanceId(instance.getId()));
		
		if (this.fileRepo.exists(spec)) {
			throw new SemanticValidationException("L'istanza di servizio ["+instance.getId()+"] non può essere disattivata in quanto ci sono ancora messaggi in coda.");
		} else {
			instance.setEnabled(false);
			return this.instanceRepo.save(instance);
		}
	}

}
