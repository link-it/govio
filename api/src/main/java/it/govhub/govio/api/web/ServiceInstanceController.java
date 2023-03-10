package it.govhub.govio.api.web;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.webjars.NotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.govio.api.assemblers.ServiceInstanceAssembler;
import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.beans.GovioServiceInstanceCreate;
import it.govhub.govio.api.beans.GovioServiceInstanceList;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.FileFilters;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceFilters;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.services.FileService;
import it.govhub.govio.api.services.ServiceInstanceService;
import it.govhub.govio.api.spec.ServiceApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity_;
import it.govhub.govregistry.commons.entity.ServiceEntity_;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.messages.PatchMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
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
	
	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstanceList> listServiceInstances(
			Direction sortDirection,
			Long serviceId,
			Long organizationId,
			String q,
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
	public ResponseEntity<GovioServiceInstance> readServiceInstance(Long id) {
		
		GovioServiceInstanceEntity instance = this.instanceRepo.findById(id)
			.orElseThrow( () -> new ResourceNotFoundException(this.instanceMessages.idNotFound(id)));
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		
		GovioServiceInstance ret = this.instanceAssembler.toModel(instance);
		return ResponseEntity.ok(ret);
	}

	@Transactional
	@Override
	public ResponseEntity<GovioServiceInstance> createServiceInstance(GovioServiceInstanceCreate src) {
		
		var serviceInstance = this.instanceAssembler.toEntity(src);
		
		serviceInstance = this.instanceRepo.save(serviceInstance);
		
		var ret = this.instanceAssembler.toEmbeddedModel(serviceInstance,  Arrays.asList(EmbedServiceInstanceEnum.values()));
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}
	
	
	@Override
	public ResponseEntity<GovioServiceInstance> updateServiceInstance(Long id, List<PatchOp> patchOp) {
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		GovioServiceInstanceEntity instance = this.instanceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.instanceMessages.idNotFound(id)));
		
		log.info("Patching service instance [{}]: {}", id,  patch);
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		GovioServiceInstanceCreate restInstance = new GovioServiceInstanceCreate();
		restInstance.
			serviceId(instance.getService().getId()).
			organizationId(instance.getOrganization().getId()).
			apiKey(instance.getApiKey()).
			templateId(instance.getTemplate().getId());
		
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
		
		return ResponseEntity.ok(this.instanceAssembler.toModel(newInstance));
	}

	
	@Override
	public ResponseEntity<Void> disableServiceInstance(Long id) {
		var instance = this.instanceRepo.findById(id)
				.orElseThrow( () -> new NotFoundException(this.instanceMessages.idNotFound(id)));
		
		this.instanceService.disableIntance(instance);
		
		return ResponseEntity.status(HttpStatus.OK).build();
	}


}
