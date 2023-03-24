package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.beans.GovioServiceInstanceCreate;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.TemplateMessages;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.web.ServiceInstanceController;
import it.govhub.govio.api.web.TemplateController;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;
import it.govhub.govregistry.readops.api.spec.ServiceApi;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;
import it.govhub.govregistry.readops.api.web.ReadServiceController;

@Component
public class ServiceInstanceAssembler extends RepresentationModelAssemblerSupport<GovioServiceInstanceEntity, GovioServiceInstance>{

	Logger log = LoggerFactory.getLogger(ServiceInstanceAssembler.class);
	
	@Autowired
	OrganizationAuthItemAssembler orgItemAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceItemAssembler;
	
	@Autowired
	TemplateAssembler templateAssembler;
	
	@Autowired
	ReadServiceRepository serviceRepo;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	TemplateRepository templateRepo;
	
	@Autowired
	ServiceMessages serviceMessages;
	
	@Autowired
	OrganizationMessages orgMessages;
	
	@Autowired
	TemplateMessages templateMessages;
	
	public ServiceInstanceAssembler() {
		super(ServiceInstanceController.class, GovioServiceInstance.class);
	}

	@Override
	public GovioServiceInstance toModel(GovioServiceInstanceEntity src) {
		log.debug("Assembling Entity [GovioServiceInstance] to model...");

		var ret = instantiateModel(src);
		BeanUtils.copyProperties(src, ret);
		ret.serviceId(src.getService().getId())
			.organizationId(src.getOrganization().getId())
			.templateId(src.getTemplate().getId());
		
		ret.add(linkTo(
				methodOn(ServiceInstanceController.class)
					.readServiceInstance(src.getId())
				).withSelfRel()).
		add(linkTo(
				methodOn(ServiceApi.class)
					.readService(src.getService().getId())
				).withRel("service")).
		add(linkTo(
				methodOn(OrganizationApi.class)
					.readOrganization(src.getOrganization().getId())
				).withRel("organization")).
		add(linkTo(
				methodOn(TemplateController.class)
					.readTemplate(src.getTemplate().getId())
				).withRel("template"));
		
		return ret;
	}

	// TODO: La List deve diventare un Set e invece di tutti questi contains(), va fatto un for sul Set
	public GovioServiceInstance toEmbeddedModel(GovioServiceInstanceEntity src, Collection<EmbedServiceInstanceEnum> embed) {
		GovioServiceInstance model = this.toModel(src);
		
		if (!CollectionUtils.isEmpty(embed)) {  
			model.setEmbedded(new HashMap<>());
			
			if (embed.contains(EmbedServiceInstanceEnum.ORGANIZATION)) {
				model.getEmbedded().put(EmbedServiceInstanceEnum.ORGANIZATION.getValue(), this.orgItemAssembler.toModel(src.getOrganization()));				
			}
			
			if (embed.contains(EmbedServiceInstanceEnum.SERVICE)) {
				model.getEmbedded().put(EmbedServiceInstanceEnum.SERVICE.getValue(), this.serviceItemAssembler.toModel(src.getService()));				
			}
			
			if (embed.contains(EmbedServiceInstanceEnum.TEMPLATE)) {
				model.getEmbedded().put(EmbedServiceInstanceEnum.TEMPLATE.getValue(), this.templateAssembler.toModel(src.getTemplate()));				
			}
		}

		return model;
	}

	public GovioServiceInstanceEntity toEntity(GovioServiceInstanceCreate src) {
		
		var service = this.serviceRepo.findById(src.getServiceId())
				.orElseThrow( () -> new SemanticValidationException(serviceMessages.idNotFound(src.getServiceId())));
		
		var organization = this.orgRepo.findById(src.getOrganizationId())
				.orElseThrow( () -> new SemanticValidationException(orgMessages.idNotFound(src.getOrganizationId())));
		
		var template = this.templateRepo.findById(src.getTemplateId())
				.orElseThrow( () -> new SemanticValidationException(templateMessages.idNotFound(src.getTemplateId())));
		
		var serviceInstance = GovioServiceInstanceEntity.builder()
			.apiKey(src.getApiKey())
			.service(service)
			.template(template)
			.organization(organization)
			.enabled(src.getEnabled())
			.build();
		
		return serviceInstance;
	}

}
