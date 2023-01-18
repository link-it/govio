package it.govhub.govio.api.web;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govio.api.assemblers.ServiceInstanceAssembler;
import it.govhub.govio.api.beans.GovioServiceInstance;
import it.govhub.govio.api.beans.GovioServiceInstanceList;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.GovioServiceInstanceFilters;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govio.api.spec.ServiceApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;

@V1RestController
public class ServiceInstanceController implements ServiceApi {
	
	@Autowired
	GovioServiceInstanceRepository serviceInstanceRepo;
	
	@Autowired
	ServiceInstanceAssembler serviceInstanceAssembler;

	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstanceList> listServiceInstances(
			Long serviceId,
			Long organizationId,
			Integer limit,
			Long offset) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.unsorted());	// TODO: Sort e autorizzazioni
		
		Specification<GovioServiceInstanceEntity> spec = GovioServiceInstanceFilters.empty();
		if (serviceId != null) {
			spec = spec.and(GovioServiceInstanceFilters.byServiceId(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(GovioServiceInstanceFilters.byOrganizationId(organizationId));
		}
		
		Page<GovioServiceInstanceEntity> instances = this.serviceInstanceRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		GovioServiceInstanceList ret = ListaUtils.buildPaginatedList(instances, pageRequest.limit, curRequest, new GovioServiceInstanceList());
		
		for (var inst : instances) {
			ret.addItemsItem(this.serviceInstanceAssembler.toModel(inst));
		}
		
		return ResponseEntity.ok(ret);
	}

	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstance> readServiceInstance(Long id) {
		// TODO autorizzazioni
		GovioServiceInstanceEntity instance = this.serviceInstanceRepo.findById(id)
			.orElseThrow( () -> new ResourceNotFoundException("Service Instance di id ["+id+"] non trovata."));
		
		GovioServiceInstance ret = this.serviceInstanceAssembler.toModel(instance);
		return ResponseEntity.ok(ret);
	}


}
