package it.govhub.govio.api.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govio.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.readops.api.assemblers.OrganizationItemAssembler;
import it.govhub.govregistry.readops.api.repository.OrganizationFilters;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;
import it.govhub.security.services.SecurityService;

@V1RestController
public class OrganizationController extends ReadOrganizationController implements OrganizationApi {
	
	@Autowired
	OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	GovioServiceInstanceRepository serviceInstanceRepo;
	
	private static Set<String> readOrganizationRoles = Set.of(
			GovioRoles.GOVIO_SYSADMIN, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_VIEWER, 
			GovioRoles.GOVIO_SERVICE_INSTANCE_EDITOR,
			GovioRoles.GOVIO_VIEWER, 
			GovioRoles.GOVIO_SENDER);

	
	@Override
	protected Set<String> getReadOrganizationRoles() {
		return new HashSet<>(readOrganizationRoles);
	}


	@Override
	public ResponseEntity<OrganizationList> listOrganizations(
			OrganizationOrdering sort,
			Direction sortDirection, 
			Integer limit,
			Long offset,
			String q, 
			List<String> withRoles,
			Boolean withServiceInstance) {
		
		Set<String> roles = getReadOrganizationRoles();
		
		if (withRoles != null) {
			roles.retainAll(withRoles);
		}
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(roles);

		if (withServiceInstance != null) {
			Set<Long> instanceOrgIds = this.serviceInstanceRepo.findAll().stream()
					.map(s -> s.getOrganization().getId())
					.collect(Collectors.toSet());
			
			if (withServiceInstance) {
				orgIds = SecurityService.restrictAuthorizations(instanceOrgIds, orgIds);
			} else {
				orgIds.removeAll(instanceOrgIds);
			}
		}

		Specification<OrganizationEntity> spec;

		if (orgIds == null) {
			// Non ho restrizioni
			spec = OrganizationFilters.empty();
		} else if (orgIds.isEmpty()) {
			// Nessuna organizzazione
			spec = OrganizationFilters.never();
		} else {
			// Filtra per le organizzazioni trovate
			spec = OrganizationFilters.byId(orgIds);
		}
		if (q != null) {
			spec = spec.and(OrganizationFilters.likeTaxCode(q).or(OrganizationFilters.likeLegalName(q)));
		}

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit,
				OrganizationFilters.sort(sort, sortDirection));

		Page<OrganizationEntity> organizations = this.orgRepo.findAll(spec, pageRequest.pageable);

		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		OrganizationList ret = ListaUtils.buildPaginatedList(organizations, pageRequest.limit, curRequest,
				new OrganizationList());
		for (OrganizationEntity org : organizations) {
			ret.addItemsItem(this.orgItemAssembler.toModel(org));
		}

		return ResponseEntity.ok(ret);
	}

}
