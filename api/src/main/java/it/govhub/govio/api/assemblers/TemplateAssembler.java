package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioTemplate;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.web.TemplateController;

@Component
public class TemplateAssembler  extends RepresentationModelAssemblerSupport<GovioTemplateEntity, GovioTemplate>{

	Logger log = LoggerFactory.getLogger(TemplateAssembler.class);
	
	public TemplateAssembler() {
		super(TemplateController.class, GovioTemplate.class);
	}

	@Override
	public GovioTemplate toModel(GovioTemplateEntity entity) {
		log.debug("Assembling Entity [GovioTemplate] to model...");

		GovioTemplate ret = new GovioTemplate();
		BeanUtils.copyProperties(entity, ret);

		ret.add(
				linkTo(
					methodOn(TemplateController.class)	.readTemplate(entity.getId()))
				.withSelfRel()
			).add(
				linkTo(
						methodOn(TemplateController.class)	.listTemplatePlaceholders(entity.getId(), null))
				.withSelfRel());
				
		return ret;
	}

	public GovioTemplateEntity toEntity(GovioTemplate model) {
		log.debug("Converting Model [GovioTemplate] to entity...");
		
		GovioTemplateEntity ret = new GovioTemplateEntity();
		BeanUtils.copyProperties(model, ret);
		
		return ret;
	}

}