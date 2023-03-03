package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.web.ServiceInstanceController;
import it.govhub.govio.api.web.TemplateController;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;
import it.govhub.govregistry.readops.api.web.ReadServiceController;

@Component
public class ServiceInstanceAssembler extends RepresentationModelAssemblerSupport<GovioServiceInstanceEntity, GovioServiceInstance>{

	Logger log = LoggerFactory.getLogger(ServiceInstanceAssembler.class);
	
	@Autowired
	OrganizationAuthItemAssembler orgItemAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceItemAssembler;
	
	public ServiceInstanceAssembler() {
		super(ServiceInstanceController.class, GovioServiceInstance.class);
	}

	@Override
	public GovioServiceInstance toModel(GovioServiceInstanceEntity src) {
		log.debug("Assembling Entity [GovioServiceInstance] to model...");

		var ret = instantiateModel(src);
		// TODO links e embed
		/*ret.organization(this.orgItemAssembler.toModel(src.getOrganization())).
				service(this.serviceItemAssembler.toModel(src.getService().getGovhubService()));*/
		
		ret.add(linkTo(
				methodOn(ServiceInstanceController.class)
					.readServiceInstance(src.getId())
				).withSelfRel()).
		add(linkTo(
				methodOn(ReadServiceController.class)
					.readService(src.getService().getId())
				).withRel("service")).
		add(linkTo(
				methodOn(ReadOrganizationController.class)
					.readOrganization(src.getOrganization().getId())
				).withRel("organization")).
		add(linkTo(
				methodOn(TemplateController.class)
					.readTemplate(src.getTemplate().getId())
				).withRel("template"));
		
		return ret;
	}

	public GovioServiceInstance toEmbeddedModel(GovioServiceInstanceEntity src, List<EmbedServiceInstanceEnum> embed) {
		
		GovioServiceInstance model = this.toModel(src);
		
		if (!CollectionUtils.isEmpty(embed))  
			model.setEmbedded(new HashMap<>());
			
/*			if (embeds.contains(EmbedPlaceholderEnum.PLACEHOLDER)) {
				item.getEmbedded().put("placeholder", this.placeholderAssembler.toModel(tp.getGovioPlaceholder()));
			}
			
			if (embeds.contains(EmbedPlaceholderEnum.TEMPLATE)) {
			item.getEmbedded().put("template",this.templateAssembler.toModel(tp.getGovioTemplate()));
			}*/

		return model;
	}

}
