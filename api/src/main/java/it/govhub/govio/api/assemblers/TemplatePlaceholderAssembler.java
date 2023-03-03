package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.govhub.govio.api.beans.EmbedPlaceholderEnum;
import it.govhub.govio.api.beans.GovioTemplatePlaceholder;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.web.TemplateController;

@Component
public class TemplatePlaceholderAssembler extends RepresentationModelAssemblerSupport<GovioTemplatePlaceholderEntity, GovioTemplatePlaceholder> {
	
	Logger log = LoggerFactory.getLogger(TemplateAssembler.class);
	
	@Autowired
	PlaceholderAssembler placeholderAssembler;
	
	@Autowired
	TemplateAssembler templateAssembler;

	public TemplatePlaceholderAssembler() {
		super(TemplateController.class, GovioTemplatePlaceholder.class);
	}

	
	@Override
	public GovioTemplatePlaceholder toModel(GovioTemplatePlaceholderEntity entity) {
		log.debug("Assembling Entity [GovioTemplatePlaceholder] to model...");
		
		var ret = new GovioTemplatePlaceholder();
		ret.setPlaceholderId(entity.getId().getGovioPlaceholder());
		BeanUtils.copyProperties(entity, ret);
		
		ret.
			add(
					linkTo(methodOn(TemplateController.class).readTemplate(entity.getId().getGovioTemplate())).
					withRel("template")).
			 add(
					linkTo(methodOn(TemplateController.class).readPlaceholder(entity.getId().getGovioPlaceholder())).
					withRel("placeholder"));
		
		return ret;
	}


	public GovioTemplatePlaceholder toEmbeddedModel(GovioTemplatePlaceholderEntity tp,	List<EmbedPlaceholderEnum> embeds) {
		
		GovioTemplatePlaceholder item = this.toModel(tp);
		
		if (!CollectionUtils.isEmpty(embeds)) {
			item.setEmbedded(new HashMap<>());
			
			if (embeds.contains(EmbedPlaceholderEnum.PLACEHOLDER)) {
				item.getEmbedded().put("placeholder", this.placeholderAssembler.toModel(tp.getGovioPlaceholder()));
			}
			
			if (embeds.contains(EmbedPlaceholderEnum.TEMPLATE)) {
			item.getEmbedded().put("template",this.templateAssembler.toModel(tp.getGovioTemplate()));
			}
		}

		return item;
	}

}
