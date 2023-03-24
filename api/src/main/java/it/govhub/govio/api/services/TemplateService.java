package it.govhub.govio.api.services;

import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.repository.TemplateRepository;

@Component
public class TemplateService {
	
	@Autowired
	TemplateRepository templateRepo;

	@Transactional
	public GovioTemplateEntity updatePlaceHolders(GovioTemplateEntity template, Set<GovioTemplatePlaceholderEntity> placeholders) {
		
		template.getGovioTemplatePlaceholders().clear();
		template.setGovioTemplatePlaceholders(placeholders);
		
		return this.templateRepo.save(template);
	}

}
