package it.govhub.govio.api.assemblers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.web.ServiceInstanceController;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;

@Component
public class ServiceInstanceAssembler extends RepresentationModelAssemblerSupport<ServiceInstanceEntity, GovioServiceInstance>{

	@Autowired
	OrganizationAuthItemAssembler orgItemAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceItemAssembler;
	
	public ServiceInstanceAssembler() {
		super(ServiceInstanceController.class, GovioServiceInstance.class);
	}

	@Override
	public GovioServiceInstance toModel(ServiceInstanceEntity src) {
		var ret = instantiateModel(src);
		
		ret.organization(this.orgItemAssembler.toModel(src.getOrganization())).
				service(this.serviceItemAssembler.toModel(src.getService().getGovhubService()));
			
		return ret;
	}

}
