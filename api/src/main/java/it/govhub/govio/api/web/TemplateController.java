package it.govhub.govio.api.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govio.api.assemblers.PlaceholderAssembler;
import it.govhub.govio.api.assemblers.TemplateAssembler;
import it.govhub.govio.api.assemblers.TemplatePlaceholderAssembler;
import it.govhub.govio.api.beans.EmbedPlaceholderEnum;
import it.govhub.govio.api.beans.GovioListTemplatePlaceholder;
import it.govhub.govio.api.beans.GovioNewPlaceholder;
import it.govhub.govio.api.beans.GovioNewTemplate;
import it.govhub.govio.api.beans.GovioNewTemplatePlaceholder;
import it.govhub.govio.api.beans.GovioPlaceholder;
import it.govhub.govio.api.beans.GovioPlaceholderList;
import it.govhub.govio.api.beans.GovioTemplate;
import it.govhub.govio.api.beans.GovioTemplateList;
import it.govhub.govio.api.beans.GovioTemplatePlaceholder;
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.entity.GovioPlaceholderEntity_;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity_;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.messages.PlaceholderMessages;
import it.govhub.govio.api.messages.TemplateMessages;
import it.govhub.govio.api.repository.GovioPlaceholderRepository;
import it.govhub.govio.api.repository.GovioTemplatePlaceholderRepository;
import it.govhub.govio.api.repository.GovioTemplateRepository;
import it.govhub.govio.api.repository.TemplatePlaceholderFilters;
import it.govhub.govio.api.spec.TemplateApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;

@V1RestController
public class TemplateController implements TemplateApi {
	// TODO: Autorizzazioni
	
	@Autowired
	GovioTemplateRepository templateRepo;
	
	@Autowired
	TemplateAssembler templateAssembler;
	
	@Autowired
	TemplateMessages templateMessages;
	
	@Autowired
	GovioPlaceholderRepository placeholderRepo;
	
	@Autowired
	PlaceholderAssembler placeholderAssembler;
	
	@Autowired
	PlaceholderMessages placeholderMessages;
	
	@Autowired
	GovioTemplatePlaceholderRepository templatePlaceholderRepo;
	
	@Autowired
	TemplatePlaceholderAssembler templatePlaceholderAssembler;
	
	@Override
	public ResponseEntity<GovioTemplate> createTemplate(GovioNewTemplate govioNewTemplate) {
		
		GovioTemplateEntity template = new GovioTemplateEntity();
		BeanUtils.copyProperties(govioNewTemplate, template);
		
		template = this.templateRepo.save(template);
		
		return ResponseEntity.
				status(HttpStatus.CREATED).
				body(this.templateAssembler.toModel(template));
	}
	
	
	@Override
	public ResponseEntity<GovioTemplate> readTemplate(Long id) {
		var template = this.templateRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(templateMessages.idNotFound(id)));
		
		return ResponseEntity.ok(this.templateAssembler.toModel(template));
	}
	
	
	@Override
	public ResponseEntity<GovioTemplateList> listTemplates(Integer limit, Long offset) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(Direction.ASC, GovioTemplateEntity_.NAME));
		
		Page<GovioTemplateEntity> templates = this.templateRepo.findAll(pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		GovioTemplateList ret = ListaUtils.buildPaginatedList(templates, pageRequest.limit, curRequest, new GovioTemplateList());
		
		for (var t : templates) {
			ret.addItemsItem(this.templateAssembler.toModel(t));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioPlaceholder> createPlaceholder(GovioNewPlaceholder govioNewPlaceholder) {
		
		var placeholder = new GovioPlaceholderEntity();
		BeanUtils.copyProperties(govioNewPlaceholder, placeholder);
		
		placeholder = this.placeholderRepo.save(placeholder);
		
		return ResponseEntity.
				status(HttpStatus.CREATED).
				body(this.placeholderAssembler.toModel(placeholder));
	}


	@Override
	public ResponseEntity<GovioPlaceholder> readPlaceholder(Long id) {
		var placeholder = this.placeholderRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.placeholderMessages.idNotFound(id)) );
		
		return ResponseEntity.ok(this.placeholderAssembler.toModel(placeholder));
	}


	@Override
	public ResponseEntity<GovioPlaceholderList> listPlaceholders(Integer limit, Long offset) {
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(Direction.DESC, GovioPlaceholderEntity_.NAME));
		
		Page<GovioPlaceholderEntity> placeholders = this.placeholderRepo.findAll(pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		GovioPlaceholderList ret = ListaUtils.buildPaginatedList(placeholders, pageRequest.limit, curRequest, new GovioPlaceholderList());
		
		for (var p : placeholders) {
			ret.addItemsItem(this.placeholderAssembler.toModel(p));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioTemplatePlaceholder> assignPlaceholder(Long templateId, Long placeholderId, GovioNewTemplatePlaceholder newTemplatePlaceholder) {
		
		// TODO Check conflitto 
		
		var template = this.templateRepo.findById(templateId)
				.orElseThrow( () -> new ResourceNotFoundException(templateMessages.idNotFound(templateId)));
		
		var placeholder = this.placeholderRepo.findById(placeholderId)
				.orElseThrow( () -> new SemanticValidationException(this.placeholderMessages.idNotFound(placeholderId)) );
		
		var templatePlaceholder = new GovioTemplatePlaceholderEntity();
		
		BeanUtils.copyProperties(newTemplatePlaceholder, templatePlaceholder);
		templatePlaceholder.setGovioTemplate(template);
		templatePlaceholder.setGovioPlaceholder(placeholder);
		
		templatePlaceholder = this.templatePlaceholderRepo.save(templatePlaceholder);
		
		return ResponseEntity.
				status(HttpStatus.CREATED).
				body(this.templatePlaceholderAssembler.toModel(templatePlaceholder));
	}


	@Override
	public ResponseEntity<GovioListTemplatePlaceholder> listTemplatePlaceholders(Long templateId, List<EmbedPlaceholderEnum> embeds) {
		
		var spec = TemplatePlaceholderFilters.byTemplateId(templateId);
		
		List<GovioTemplatePlaceholderEntity> templatePlaceholders = this.templatePlaceholderRepo.findAll(spec);
		
		GovioListTemplatePlaceholder ret = new GovioListTemplatePlaceholder();
		
		for (var tp : templatePlaceholders) {
			GovioTemplatePlaceholder item = this.templatePlaceholderAssembler.toEmbeddedModel(tp, embeds);
			ret.addItemsItem(item);
		}
		
		return ResponseEntity.ok(ret);
	}

}
