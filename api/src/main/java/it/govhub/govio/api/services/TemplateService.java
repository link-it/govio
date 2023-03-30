package it.govhub.govio.api.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.assemblers.TemplatePlaceholderAssembler;
import it.govhub.govio.api.beans.EmbedPlaceholderEnum;
import it.govhub.govio.api.beans.GovioListTemplatePlaceholder;
import it.govhub.govio.api.beans.GovioTemplatePlaceholder;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity_;
import it.govhub.govio.api.repository.TemplatePlaceholderRepository;
import it.govhub.govio.api.repository.TemplateRepository;

@Component
public class TemplateService {
	
	@Autowired
	TemplateRepository templateRepo;
	
	@Autowired
	TemplatePlaceholderRepository templatePlaceholderRepo;
	
	@Autowired
	TemplatePlaceholderAssembler templatePlaceholderAssembler;
	
	Logger log = LoggerFactory.getLogger(TemplateService.class);

	@Transactional
	public GovioTemplateEntity updatePlaceHolders(GovioTemplateEntity template, Set<GovioTemplatePlaceholderEntity> placeholders) {
		log.info("Setting new placeholders for template [{}]", template.getId());
		
		template.getGovioTemplatePlaceholders().clear();
		template.getGovioTemplatePlaceholders().addAll(placeholders);
		
		template = this.templateRepo.save(template);
		
		return this.templateRepo.save(template);
	}

	// TODO: Sostituire con entity graph , togliere Transactional e fare fare il lavoro di conversione al controller
	@Transactional
	public GovioListTemplatePlaceholder listTemplatePlaceholders(Specification<GovioTemplatePlaceholderEntity> spec,	Sort by, List<EmbedPlaceholderEnum> embeds) {
		
		List<GovioTemplatePlaceholderEntity> templatePlaceholders = this.templatePlaceholderRepo.findAll(spec, Sort.by(Direction.ASC, GovioTemplatePlaceholderEntity_.POSITION));
		
		GovioListTemplatePlaceholder ret = new GovioListTemplatePlaceholder();
		
		ret.setItems(new ArrayList<>());
		for (var tp : templatePlaceholders) {
			GovioTemplatePlaceholder item = this.templatePlaceholderAssembler.toEmbeddedModel(tp, embeds);
			ret.addItemsItem(item);
		}
		
		return ret;
	}

}
