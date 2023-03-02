package it.govhub.govio.api.web;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govio.api.assemblers.ServiceInstanceAssembler;
import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.beans.GovioServiceInstanceCreate;
import it.govhub.govio.api.beans.GovioServiceInstanceList;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.ServiceInstanceFilters;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.spec.ServiceApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.security.services.SecurityService;

@V1RestController
public class ServiceInstanceController implements ServiceApi {
	
	@Autowired
	ServiceInstanceRepository serviceInstanceRepo;
	
	@Autowired
	ServiceInstanceAssembler instanceAssembler;
	
	@Autowired
	ServiceInstanceMessages instanceMessages;
	
	@Autowired
	SecurityService authService;

	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstanceList> listServiceInstances(
			Direction sortDirection,
			Long serviceId,
			Long organizationId,
			String serviceQ,
			String organizationQ,
			Integer limit,
			Long offset,
			 List<EmbedServiceInstanceEnum> embed) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.by(Direction.DESC, GovioServiceInstanceEntity_.ID));
		
		Specification<GovioServiceInstanceEntity> spec = ServiceInstanceFilters.empty();

		// Pesco servizi e autorizzazioni che l'utente può leggere
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		Set<Long> serviceIds = this.authService.listAuthorizedServices(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		
		if (orgIds != null) {
			spec = spec.and(ServiceInstanceFilters.byOrganizationIds(orgIds));
		}
		if (serviceIds != null) {
			spec = spec.and(ServiceInstanceFilters.byServiceIds(serviceIds));
		}
		if (serviceId != null) {
			spec = spec.and(ServiceInstanceFilters.byServiceId(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(ServiceInstanceFilters.byOrganizationId(organizationId));
		}
		
		Page<GovioServiceInstanceEntity> instances = this.serviceInstanceRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		GovioServiceInstanceList ret = ListaUtils.buildPaginatedList(instances, pageRequest.limit, curRequest, new GovioServiceInstanceList());
		
		for (var inst : instances) {
			ret.addItemsItem(this.instanceAssembler.toModel(inst));
		}
		
		return ResponseEntity.ok(ret);
	}

	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstance> readServiceInstance(Long id) {
		
		GovioServiceInstanceEntity instance = this.serviceInstanceRepo.findById(id)
			.orElseThrow( () -> new ResourceNotFoundException(this.instanceMessages.idNotFound(id)));
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR, GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER);
		
		GovioServiceInstance ret = this.instanceAssembler.toModel(instance);
		return ResponseEntity.ok(ret);
	}

	@Override
	public ResponseEntity<GovioServiceInstance> createServiceInstance(
			@Valid GovioServiceInstanceCreate govioServiceInstanceCreate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Void> disableServiceInstance(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<GovioServiceInstance> updateServiceInstance(Long id, List<PatchOp> patchOp) {
		// TODO Auto-generated method stub
		return null;
	}


}
