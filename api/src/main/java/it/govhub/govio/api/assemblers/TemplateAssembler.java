package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioTemplate;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.web.TemplateController;

@Component
public class TemplateAssembler  extends RepresentationModelAssemblerSupport<GovioTemplateEntity, GovioTemplate>{

	public TemplateAssembler() {
		super(TemplateController.class, GovioTemplate.class);
	}

	@Override
	public GovioTemplate toModel(GovioTemplateEntity entity) {
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

}
