/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
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

import it.govhub.govio.api.assemblers.ServiceInstanceAssembler;
import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.beans.GovioServiceInstanceCreate;
import it.govhub.govio.api.beans.GovioServiceInstanceFull;
import it.govhub.govio.api.beans.GovioServiceInstanceList;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceFilters;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.services.ServiceInstanceService;
import it.govhub.govio.api.spec.ServiceApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceList;
import it.govhub.govregistry.commons.api.beans.ServiceOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.PatchMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.web.ReadServiceController;
import it.govhub.security.services.SecurityService;

@V1RestController
public class ServiceInstanceController implements ServiceApi {
	
	Logger log = LoggerFactory.getLogger(ServiceInstanceController.class);
	
	@Autowired
	ServiceInstanceAssembler instanceAssembler;
	
	@Autowired
	ServiceInstanceMessages instanceMessages;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	ServiceInstanceRepository instanceRepo;

	@Autowired
	FileRepository fileRepo;
	
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	Validator validator;
	
	@Autowired
	ServiceInstanceService instanceService;
	
	@Autowired
	ReadServiceController readServiceController;
	
	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstanceList> listServiceInstances(
			Direction sortDirection,
			Long serviceId,
			Long organizationId,
			String  ioServiceId,
			String q,
			Boolean enabled,
			Integer limit,
			Long offset,
			 List<EmbedServiceInstanceEnum> embed) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(sortDirection, GovioServiceInstanceEntity_.ORGANIZATION +"."+OrganizationEntity_.LEGAL_NAME, GovioServiceInstanceEntity_.SERVICE + "." + ServiceEntity_.NAME));
		
		Specification<GovioServiceInstanceEntity> spec = ServiceInstanceFilters.empty();

		// Pesco servizi e autorizzazioni che l'utente può leggere
		Set<Long> serviceIds = this.authService.listAuthorizedServices(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		if (serviceId != null && serviceIds != null) { 
			serviceIds.retainAll(Set.of(serviceId));
		}
		else if (serviceId != null && serviceIds == null) {
			serviceIds = Set.of(serviceId);
		}

		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		if (organizationId != null && orgIds != null) {
			orgIds.retainAll(Set.of(organizationId));
		}
		else if (organizationId != null && orgIds == null) {
			orgIds = Set.of(organizationId);
		}
		
		if (orgIds != null) {
			spec = spec.and(ServiceInstanceFilters.byOrganizationIds(orgIds));
		}
		if (serviceIds != null) {
			spec = spec.and(ServiceInstanceFilters.byServiceIds(serviceIds));
		}
		if (!StringUtils.isBlank(q)) {
			spec = spec.and(
						ServiceInstanceFilters.likeServiceName(q).
						or(ServiceInstanceFilters.likeTemplateName(q)).
						or(ServiceInstanceFilters.likeOrganizationName(q)).
						or(ServiceInstanceFilters.likeOrganizationTaxCode(q))
					);
		}
		if (enabled != null) {
			spec = spec.and(ServiceInstanceFilters.isEnabled(enabled));
		}
		if (! StringUtils.isBlank(ioServiceId)) {
			spec = spec.and(ServiceInstanceFilters.byIoServiceId(ioServiceId));
		}
		
		Page<GovioServiceInstanceEntity> instances = this.instanceRepo.findAll(spec, pageRequest.pageable);
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		GovioServiceInstanceList ret = ListaUtils.buildPaginatedList(instances, pageRequest.limit, curRequest, new GovioServiceInstanceList());
		
		for (var inst : instances) {
			ret.addItemsItem(this.instanceAssembler.toEmbeddedModel(inst,embed));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstanceFull> readServiceInstance(Long id) {
		
		GovioServiceInstanceEntity instance = this.instanceRepo.findById(id)
			.orElseThrow( () -> new ResourceNotFoundException(this.instanceMessages.idNotFound(id)));
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		
		GovioServiceInstance instanceRest = this.instanceAssembler.toEmbeddedModel(instance, Arrays.asList(EmbedServiceInstanceEnum.values()));
		GovioServiceInstanceFull fullInstanceRest = new GovioServiceInstanceFull();
		
		BeanUtils.copyProperties(instanceRest, fullInstanceRest);
		fullInstanceRest.setApiKey(instance.getApiKey());
		
		return ResponseEntity.ok(fullInstanceRest);
	}

	
	@Transactional
	@Override
	public ResponseEntity<GovioServiceInstanceFull> createServiceInstance(GovioServiceInstanceCreate src) {
		
		log.info("Creating new Service Instance: {}", src);
		
		this.authService.hasAnyOrganizationAuthority(src.getOrganizationId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR);
		this.authService.hasAnyServiceAuthority(src.getServiceId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR);
		
		var spec = ServiceInstanceFilters.byOrganizationId(src.getOrganizationId()).
				and(ServiceInstanceFilters.byServiceId(src.getServiceId())).
				and(ServiceInstanceFilters.byTemplateId(src.getTemplateId()));
		
		this.instanceRepo.findOne(spec).ifPresent( inst -> {
					throw new ConflictException(this.instanceMessages.conflict());
			});				
		
		var serviceInstance = this.instanceAssembler.toEntity(src);
		
		serviceInstance = this.instanceRepo.save(serviceInstance);
		
		GovioServiceInstance instanceRest = this.instanceAssembler.toEmbeddedModel(serviceInstance, Arrays.asList(EmbedServiceInstanceEnum.values()));
		GovioServiceInstanceFull fullInstanceRest = new GovioServiceInstanceFull();
		
		BeanUtils.copyProperties(instanceRest, fullInstanceRest);
		fullInstanceRest.setApiKey(serviceInstance.getApiKey());
		
		return ResponseEntity.status(HttpStatus.CREATED).body(fullInstanceRest);
	}
	
	
	@Override
	public ResponseEntity<GovioServiceInstanceFull> updateServiceInstance(Long id, List<PatchOp> patchOp) {
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		GovioServiceInstanceEntity instance = this.instanceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.instanceMessages.idNotFound(id)));
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(),  GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR);
		
		log.info("Patching service instance [{}]: {}", id,  patch);
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		this.instanceAssembler.toModel(instance);
		GovioServiceInstanceCreate restInstance = new GovioServiceInstanceCreate();
		restInstance.
			serviceId(instance.getService().getId()).
			organizationId(instance.getOrganization().getId()).
			apiKey(instance.getApiKey()).
			templateId(instance.getTemplate().getId()).
			enabled(instance.getEnabled()).
			ioServiceId(instance.getIoServiceId());
		
		JsonNode newJsonInstance;
		try {
			JsonNode jsonInstance = this.objectMapper.convertValue(restInstance, JsonNode.class);
			newJsonInstance = patch.apply(jsonInstance);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto User, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		GovioServiceInstanceCreate updatedInstance;
		try {
			updatedInstance= this.objectMapper.treeToValue(newJsonInstance, GovioServiceInstanceCreate.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedInstance== null) {
			throw new BadRequestException(PatchMessages.VOID_OBJECT_PATCH);
		}
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedInstance, updatedInstance.getClass().getName());
		validator.validate(updatedInstance, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedInstance.getApiKey(), "apiKey");
		
		// Dall'oggetto REST passo alla entity
		GovioServiceInstanceEntity newInstance = this.instanceAssembler.toEntity(updatedInstance);
		newInstance.setId(id);
		
		newInstance = this.instanceService.replaceInstance(instance, newInstance);
		
		GovioServiceInstance instanceRest = this.instanceAssembler.toModel(newInstance);		
		GovioServiceInstanceFull fullInstanceRest = new GovioServiceInstanceFull();
		
		BeanUtils.copyProperties(instanceRest, fullInstanceRest);
		fullInstanceRest.setApiKey(newInstance.getApiKey());
		
		return ResponseEntity.ok(fullInstanceRest);
	}


	@Override
	public ResponseEntity<Resource> downloadServiceLogo(Long id) {
		return this.readServiceController.downloadServiceLogo(id);
	}

	@Override
	public ResponseEntity<Resource> downloadServiceLogoMiniature(Long id) {
		return this.readServiceController.downloadServiceLogoMiniature(id);
	}

	@Override
	public ResponseEntity<ServiceList> listServices(ServiceOrdering sort, Direction sortDirection, Integer limit,	Long offset, String q, List<String> withRoles) {
		return this.readServiceController.listServices(sort, sortDirection, limit, offset, q, withRoles);
	}

	
	@Override
	public ResponseEntity<Service> readService(Long id) {
		return this.readServiceController.readService(id);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
