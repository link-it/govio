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
import it.govhub.govio.api.beans.GovioServiceInstanceList;
import it.govhub.govio.api.entity.ServiceInstanceEntity;
import it.govhub.govio.api.repository.ServiceInstanceFilters;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.spec.ServiceApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;

@V1RestController
public class ServiceInstanceController implements ServiceApi {
	
	@Autowired
	ServiceInstanceRepository serviceInstanceRepo;
	
	@Autowired
	ServiceInstanceAssembler serviceInstanceAssembler;

	@Override
	@Transactional
	public ResponseEntity<GovioServiceInstanceList> listServiceInstances(
			Long serviceId,
			Long organizationId,
			Integer limit,
			Long offset) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.unsorted());	// TODO: Sort
		
		Specification<ServiceInstanceEntity> spec = ServiceInstanceFilters.empty();
		if (serviceId != null) {
			spec = spec.and(ServiceInstanceFilters.byServiceId(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(ServiceInstanceFilters.byOrganizationId(organizationId));
		}
		
		Page<ServiceInstanceEntity> instances = this.serviceInstanceRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		GovioServiceInstanceList ret = ListaUtils.costruisciListaPaginata(instances, pageRequest.limit, curRequest, new GovioServiceInstanceList());
		
		for (var inst : instances) {
			ret.addItemsItem(this.serviceInstanceAssembler.toModel(inst));
		}
		
		return ResponseEntity.ok(ret);
	}



}
