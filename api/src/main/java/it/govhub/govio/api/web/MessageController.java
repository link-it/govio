package it.govhub.govio.api.web;

import java.time.OffsetDateTime;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.assemblers.MessageAssembler;
import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessageCreate;
import it.govhub.govio.api.beans.GovioMessageList;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioMessageEntity.Status;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.MessageMessages;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.GovioMessageFilters;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govio.api.services.MessageService;
import it.govhub.govio.api.spec.MessageApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.security.services.SecurityService;

@V1RestController
public class MessageController implements MessageApi {
	
	@Autowired
	MessageService messageService;
	
	@Autowired
	MessageAssembler messageAssembler;

	@Autowired
	GovioServiceInstanceRepository serviceInstanceRepo;
	
	@Autowired
	GovioMessageRepository messageRepo;
	
	@Autowired
	ServiceInstanceMessages sinstanceMessages;
	
	@Autowired
	MessageMessages messageMessages;
	
	@Override
	public ResponseEntity<GovioMessageList> listMessages(
			OffsetDateTime scheduledExpeditionDateFrom,
			OffsetDateTime scheduledExpeditionDateTo,
			OffsetDateTime expeditionDateFrom,
			OffsetDateTime expeditionDateTo,
			String taxCode,
			Long serviceId,
			Long organizationId,
			Integer limit,
			Long offset) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, Sort.unsorted());	// TODO: Sort e autorizzazioni
		
		Specification<GovioMessageEntity> spec = GovioMessageFilters.empty();
		if (scheduledExpeditionDateFrom != null) {
			spec = spec.and(GovioMessageFilters.fromScheduledExpeditionDate(scheduledExpeditionDateFrom));
		}
		if (scheduledExpeditionDateTo != null) {
			spec = spec.and(GovioMessageFilters.toScheduledExpeditionDate(scheduledExpeditionDateTo));
		}
		if (expeditionDateFrom != null) {
			spec = spec.and(GovioMessageFilters.fromExpeditionDate(expeditionDateFrom));
		}
		if (expeditionDateTo != null) {
			spec = spec.and(GovioMessageFilters.toExpeditionDate(expeditionDateTo));
		}
		if (taxCode != null) {
			spec = spec.and(GovioMessageFilters.byTaxCode(taxCode));
		}
		if (serviceId != null) {
			spec = spec.and(GovioMessageFilters.byServiceId(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(GovioMessageFilters.byOrganizationId(organizationId));
		}
		
		// TODO: Autorizzazioni
		GovioMessageList ret = this.messageService.listMessages(spec, pageRequest);
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioMessage> readMessage(Long id) {
		
		// TODO: Autorizzazioni
		return ResponseEntity.ok(this.messageService.readMessage(id));
	}


	@Transactional
	@Override
	public ResponseEntity<GovioMessage> sendMessage(Long serviceInstance, GovioMessageCreate govioMessageCreate) {
		
		// TODO: Autorizzazioni
		
		GovioServiceInstanceEntity instance = this.serviceInstanceRepo.findById(serviceInstance)
				.orElseThrow( () -> new SemanticValidationException(this.sinstanceMessages.idNotFound(serviceInstance)));
		
		var entity = new GovioMessageEntity();
		
		BeanUtils.copyProperties(govioMessageCreate, entity);
		
		entity.setGovioServiceInstance(instance);
		entity.setSender(SecurityService.getPrincipal());
		entity.setCreationDate(OffsetDateTime.now());
		entity.setStatus(Status.SCHEDULED);
		
		entity = this.messageRepo.save(entity);
		
		GovioMessage ret = this.messageAssembler.toModel(entity);
		
		return ResponseEntity.ok(ret);
	}

	
}

















