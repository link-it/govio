/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govio.api.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

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
import it.govhub.govio.api.beans.GovioTemplatePlaceholderUpdateList;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.entity.GovioPlaceholderEntity_;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity_;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity_;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderKey;
import it.govhub.govio.api.messages.PlaceholderMessages;
import it.govhub.govio.api.messages.TemplateMessages;
import it.govhub.govio.api.repository.PlaceholderFilters;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.repository.TemplateFilters;
import it.govhub.govio.api.repository.TemplatePlaceholderFilters;
import it.govhub.govio.api.repository.TemplatePlaceholderRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.services.TemplateService;
import it.govhub.govio.api.spec.TemplateApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.messages.PatchMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.security.services.SecurityService;

@V1RestController
public class TemplateController implements TemplateApi {
	
	@Autowired
	TemplateRepository templateRepo;
	
	@Autowired
	TemplateAssembler templateAssembler;
	
	@Autowired
	TemplateMessages templateMessages;
	
	@Autowired
	PlaceholderRepository placeholderRepo;
	
	@Autowired
	PlaceholderAssembler placeholderAssembler;
	
	@Autowired
	PlaceholderMessages placeholderMessages;
	
	@Autowired
	TemplatePlaceholderRepository templatePlaceholderRepo;
	
	@Autowired
	TemplatePlaceholderAssembler templatePlaceholderAssembler;
	
	@Autowired
	Validator validator;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	TemplateService templateService;
	
	Logger log = LoggerFactory.getLogger(TemplateController.class);
	
	@Override
	public ResponseEntity<GovioTemplate> createTemplate(GovioNewTemplate govioNewTemplate) {
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SYSADMIN);
		log.info("Creating new template: {}", govioNewTemplate);
		
		GovioTemplateEntity template = new GovioTemplateEntity();
		BeanUtils.copyProperties(govioNewTemplate, template);
		
		template = this.templateRepo.save(template);
		
		return ResponseEntity.
				status(HttpStatus.CREATED).
				body(this.templateAssembler.toModel(template));
	}
	
	
	@Override
	public ResponseEntity<GovioTemplate> readTemplate(Long id) {
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		
		var template = this.templateRepo.findById(id).orElse(null);
		
		if (template == null) {
				throw new ResourceNotFoundException(templateMessages.idNotFound(id));
		}
		
		return ResponseEntity.ok(this.templateAssembler.toModel(template));
	}
	
	
	@Override
	public ResponseEntity<GovioTemplateList> listTemplates(Direction sortDirection, Integer limit, Long offset, String q) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(sortDirection, GovioTemplateEntity_.NAME));
		Specification<GovioTemplateEntity> spec = TemplateFilters.empty();
		if (!StringUtils.isBlank(q)) {
			spec = TemplateFilters.likeDescription(q).
					or(TemplateFilters.likeName(q));
		}
		

		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		Page<GovioTemplateEntity> templates = this.templateRepo.findAll(spec, pageRequest.pageable);		
		GovioTemplateList ret = ListaUtils.buildPaginatedList(templates, pageRequest.limit, curRequest, new GovioTemplateList());
		
		for (var t : templates) {
			ret.addItemsItem(this.templateAssembler.toModel(t));
		}
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioPlaceholder> createPlaceholder(GovioNewPlaceholder govioNewPlaceholder) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SYSADMIN);
		
		log.info("Creating new placeholder: {}", govioNewPlaceholder);
		
		var conflict = this.placeholderRepo.findOne(PlaceholderFilters.byName(govioNewPlaceholder.getName()));
		if (conflict.isPresent()) {
			throw new ConflictException(this.placeholderMessages.conflict("name", govioNewPlaceholder.getName()));
		}
		
		var placeholder = new GovioPlaceholderEntity();
		BeanUtils.copyProperties(govioNewPlaceholder, placeholder);
		
		placeholder = this.placeholderRepo.save(placeholder);
		return ResponseEntity.
				status(HttpStatus.CREATED).
				body(this.placeholderAssembler.toModel(placeholder));
	}


	@Override
	public ResponseEntity<GovioPlaceholder> readPlaceholder(Long id) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		
		var placeholder = this.placeholderRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.placeholderMessages.idNotFound(id)) );
		
		return ResponseEntity.ok(this.placeholderAssembler.toModel(placeholder));
	}


	@Override
	public ResponseEntity<GovioPlaceholderList> listPlaceholders(Integer limit, Long offset, String q) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(Direction.DESC, GovioPlaceholderEntity_.NAME));
		
		Specification<GovioPlaceholderEntity> spec = PlaceholderFilters.empty();
		if (!StringUtils.isBlank(q)) {
			spec = PlaceholderFilters.likeDescription(q).	or(PlaceholderFilters.likeName(q));
					
		}
		
		Page<GovioPlaceholderEntity> placeholders = this.placeholderRepo.findAll(spec, pageRequest.pageable);
		
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
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,  GovioRoles.GOVIO_SYSADMIN);
		
		log.info("Assigning placeholder [{}] to template [{}]: {}", placeholderId, templateId, newTemplatePlaceholder);
		
		var template = this.templateRepo.findById(templateId)
				.orElseThrow( () -> new ResourceNotFoundException(templateMessages.idNotFound(templateId)));
		
		var placeholder = this.placeholderRepo.findById(placeholderId)
				.orElseThrow( () -> new SemanticValidationException(this.placeholderMessages.idNotFound(placeholderId)) );
		
		var spec = TemplatePlaceholderFilters.byTemplateId(templateId)
				.and( 
						TemplatePlaceholderFilters.byPlaceholderId(placeholderId).or(TemplatePlaceholderFilters.byPosition(newTemplatePlaceholder.getPosition()))
					);
		
		if (this.templatePlaceholderRepo.exists(spec)) {
			throw new ConflictException("Placeholder with the same id: ["+placeholderId+"] or position: ["+newTemplatePlaceholder.getPosition()+"] already associated with the template");
		}
		
		var templatePlaceholder = new GovioTemplatePlaceholderEntity();
		
		var id = new GovioTemplatePlaceholderKey(placeholder.getId(), template.getId());
		
		templatePlaceholder.setId(id);
		BeanUtils.copyProperties(newTemplatePlaceholder, templatePlaceholder);
		
		templatePlaceholder = this.templatePlaceholderRepo.save(templatePlaceholder);
		
		return ResponseEntity.
				status(HttpStatus.CREATED).
				body(this.templatePlaceholderAssembler.toModel(templatePlaceholder));
	}
	
	
	@Override
	public ResponseEntity<GovioListTemplatePlaceholder> updateTemplatePlaceholders(Long id, GovioTemplatePlaceholderUpdateList placeholders) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,  GovioRoles.GOVIO_SYSADMIN);
		
		final GovioTemplateEntity template = this.templateRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.templateMessages.idNotFound(id)));
		
		Set<Long> requestedIds = new HashSet<>();
		Set<Integer> positions = new HashSet<>();
		
		// Validazione semantica, verifichiamo che i placeholder esistano, e che non  cisiano duplicati
		placeholders.getItems()
				.stream()
				.forEach( p -> {
					if (requestedIds.contains(p.getPlaceholderId())) {
						throw new SemanticValidationException("Duplicated placeholder_id: ["+p.getPlaceholderId()+"] for template_id ["+id+"]");
					}
					if (positions.contains(p.getPosition())) {
						throw new SemanticValidationException("Duplicated placeholder position: ["+p.getPosition()+"]");
					}
					requestedIds.add(p.getPlaceholderId());
					positions.add(p.getPosition());
				});
		
		List<GovioPlaceholderEntity> placeholdersFound = this.placeholderRepo.findAll(PlaceholderFilters.byIds(requestedIds));
		Set<Long> foundIds = placeholdersFound.stream().map(GovioPlaceholderEntity::getId).collect(Collectors.toSet());
		requestedIds.removeAll(foundIds);
		
		if (!requestedIds.isEmpty()) {
			throw new SemanticValidationException(this.placeholderMessages.idsNotFound(requestedIds));
		}

		Set<GovioTemplatePlaceholderEntity> templatePlaceholders = placeholders.getItems().
			stream().
			map( p -> {
				var templatePlaceholder = new GovioTemplatePlaceholderEntity();
				templatePlaceholder.setId(new GovioTemplatePlaceholderKey(p.getPlaceholderId(), template.getId()));
				BeanUtils.copyProperties(p, templatePlaceholder);
				return templatePlaceholder;
			}).
			collect(Collectors.toSet());
		
		this.templateService.updatePlaceHolders(template, templatePlaceholders);
		
		return this.listTemplatePlaceholders(id, null);
	}
	


	@Override
	public ResponseEntity<GovioListTemplatePlaceholder> listTemplatePlaceholders(Long templateId, List<EmbedPlaceholderEnum> embeds) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		
		var template = this.templateRepo.findById(templateId).orElse(null);
		if (template == null) {
				throw new ResourceNotFoundException(this.templateMessages.idNotFound(templateId));
		}
		
		var spec = TemplatePlaceholderFilters.byTemplateId(templateId);
		
		GovioListTemplatePlaceholder ret = this.templateService.listTemplatePlaceholders(spec,Sort.by(Direction.ASC, GovioTemplatePlaceholderEntity_.POSITION), embeds );  

		return ResponseEntity.ok(ret);
	}


	@Transactional
	@Override
	public ResponseEntity<GovioTemplate> updateTemplate(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,  GovioRoles.GOVIO_SYSADMIN);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		log.info("Patching template [{}]: {}", id, patch);
		
		GovioTemplateEntity template = this.templateRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.templateMessages.idNotFound(id)));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		GovioTemplate restTemplate = this.templateAssembler.toModel(template);
		JsonNode jsonTemplate = this.objectMapper.convertValue(restTemplate, JsonNode.class);
		
		JsonNode newJsonTemplate;
		try {
			newJsonTemplate = patch.apply(jsonTemplate);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto Template, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		GovioTemplate updatedTemplate;
		try {
			updatedTemplate = this.objectMapper.treeToValue(newJsonTemplate, GovioTemplate.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedTemplate == null) {
			throw new BadRequestException(PatchMessages.VOID_OBJECT_PATCH);
		}
		updatedTemplate.setId(id);
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedTemplate, updatedTemplate.getClass().getName());
		validator.validate(updatedTemplate, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedTemplate.getName(), "name");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedTemplate.getDescription(), "description");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedTemplate.getSubject(), "subject");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedTemplate.getMessageBody(), "message_body");
		
		// Dall'oggetto REST passo alla entity
		GovioTemplateEntity newTemplate = this.templateAssembler.toEntity(updatedTemplate);
		newTemplate.setGovioTemplatePlaceholders(template.getGovioTemplatePlaceholders());

		newTemplate = this.templateRepo.save(newTemplate);
		
		return ResponseEntity.ok(this.templateAssembler.toModel(newTemplate));
	}
	
	
	@Override
	public ResponseEntity<Void> removeTemplatePlaceholder(Long templateId, Long placeholderId) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SYSADMIN);
		
		var template = this.templateRepo.findById(templateId)
				.orElseThrow( () -> new ResourceNotFoundException(this.templateMessages.idNotFound(templateId)) );
		
		var placeholder = IterableUtils.find(template.getGovioTemplatePlaceholders(), t -> t.getGovioPlaceholder().getId().equals(placeholderId));
		if (placeholder == null) {
			throw new ResourceNotFoundException(this.placeholderMessages.idNotFound(placeholderId));
		}
		this.templatePlaceholderRepo.delete(placeholder);
		template.getGovioTemplatePlaceholders().remove(placeholder);
		template = this.templateRepo.save(template);

		// Scorro tutti i placeholders e se successivi al corrente, decremento la posizione
		for(var p : template.getGovioTemplatePlaceholders() ) {
			if (p.getPosition() > placeholder.getPosition()) {
				p.setPosition(p.getPosition()-1);
			}
		}
		this.templateRepo.save(template);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}


	@Override
	public ResponseEntity<GovioPlaceholder> updatePlaceholder(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyRole(GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,  GovioRoles.GOVIO_SYSADMIN);

		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		log.info("Patching placeholder [{}]: {}", id, patch);
		
		GovioPlaceholderEntity placeholder= this.placeholderRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.placeholderMessages.idNotFound(id)));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		GovioPlaceholder restPlaceholder= this.placeholderAssembler.toModel(placeholder);
		JsonNode jsonTemplate = this.objectMapper.convertValue(restPlaceholder, JsonNode.class);
		
		JsonNode newJsonTemplate;
		try {
			newJsonTemplate = patch.apply(jsonTemplate);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto Template, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		GovioPlaceholder updatedPlaceholder;
		try {
			updatedPlaceholder = this.objectMapper.treeToValue(newJsonTemplate, GovioPlaceholder.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedPlaceholder == null) {
			throw new BadRequestException(PatchMessages.VOID_OBJECT_PATCH);
		}
		updatedPlaceholder.setId(id);
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedPlaceholder, updatedPlaceholder.getClass().getName());
		validator.validate(updatedPlaceholder, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedPlaceholder.getName(), "name");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedPlaceholder.getDescription(), "description");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedPlaceholder.getExample(), "example");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedPlaceholder.getPattern(), "pattern");
		
		// Controllo un eventuale duplicato.
		var conflict = this.placeholderRepo.findOne(PlaceholderFilters.byName(updatedPlaceholder.getName()))
					.orElse(null);
		if (conflict != null && conflict.getId() != updatedPlaceholder.getId()) {
			throw new ConflictException(this.placeholderMessages.conflict("name", updatedPlaceholder.getName()));
		}
		
		
		// Dall'oggetto REST passo alla entity
		GovioPlaceholderEntity newPlaceholder = this.placeholderAssembler.toEntity(updatedPlaceholder);

		newPlaceholder = this.placeholderRepo.save(newPlaceholder);
		
		return ResponseEntity.ok(this.placeholderAssembler.toModel(newPlaceholder));
	}


}
