package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioPlaceholder;
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.web.TemplateController;

@Component
public class PlaceholderAssembler extends RepresentationModelAssemblerSupport<GovioPlaceholderEntity, GovioPlaceholder> {

	public PlaceholderAssembler() {
		super(TemplateController.class, GovioPlaceholder.class);
	}

	@Override
	public GovioPlaceholder toModel(GovioPlaceholderEntity entity) {
		var ret = new GovioPlaceholder();
		BeanUtils.copyProperties(entity,ret);
		
		ret.add(
				linkTo(methodOn(TemplateController.class).readPlaceholder(ret.getId()))
				.withSelfRel());
		
		return ret;
	}

}
