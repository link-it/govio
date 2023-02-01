package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioTemplatePlaceholder;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.web.TemplateController;

@Component
public class TemplatePlaceholderAssembler extends RepresentationModelAssemblerSupport<GovioTemplatePlaceholderEntity, GovioTemplatePlaceholder> {

	public TemplatePlaceholderAssembler() {
		super(TemplateController.class, GovioTemplatePlaceholder.class);
	}

	
	@Override
	public GovioTemplatePlaceholder toModel(GovioTemplatePlaceholderEntity entity) {
		
		var ret = new GovioTemplatePlaceholder();
		BeanUtils.copyProperties(entity, ret);
		
		ret.
			add(
					linkTo(methodOn(TemplateController.class).readTemplate(entity.getGovioTemplate().getId())).
					withRel("template")).
			 add(
					linkTo(methodOn(TemplateController.class).readPlaceholder(entity.getGovioPlaceholder().getId())).
					withRel("placeholder"));
		
		return ret;
	}

}
