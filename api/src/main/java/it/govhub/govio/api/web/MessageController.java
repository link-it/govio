package it.govhub.govio.api.web;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.assemblers.MessageAssembler;
import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessageList;
import it.govhub.govio.api.beans.GovioNewMessage;
import it.govhub.govio.api.beans.GovioNewMessageAllOfPlaceholders;
import it.govhub.govio.api.beans.MessageOrdering;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.MessageMessages;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.GovioMessageFilters;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govio.api.security.GovioRoles;
import it.govhub.govio.api.services.MessageService;
import it.govhub.govio.api.spec.MessageApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.security.services.SecurityService;
import it.govio.template.BaseMessage;

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
	
	@Autowired
	SecurityService authService;
	
	@Override
	public ResponseEntity<GovioMessageList> listMessages(
			MessageOrdering orderBy,
			Direction sortDirection,
			OffsetDateTime scheduledExpeditionDateFrom,
			OffsetDateTime scheduledExpeditionDateTo,
			OffsetDateTime expeditionDateFrom,
			OffsetDateTime expeditionDateTo,
			String taxCode,
			Long serviceId,
			Long organizationId,
			Integer limit,
			Long offset) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, GovioMessageFilters.sort(orderBy, sortDirection));
		
		// Pesco servizi e autorizzazioni che l'utente può leggere
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER);
		Set<Long> serviceIds = this.authService.listAuthorizedServices(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER);
		
		Specification<GovioMessageEntity> spec = GovioMessageFilters.empty();
		
		if (orgIds != null) {
			spec = spec.and(GovioMessageFilters.byOrganizationIds(orgIds));
		}
		if (serviceIds != null) {
			spec = spec.and(GovioMessageFilters.byServiceIds(serviceIds));
		}
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
		
		GovioMessageList ret = this.messageService.listMessages(spec, pageRequest);
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioMessage> readMessage(Long id) {
		
		return ResponseEntity.ok(this.messageService.readMessage(id));
	}

	@Transactional
	@Override
	public ResponseEntity<GovioMessage> sendMessage(Long serviceInstance, GovioNewMessage govioNewMessage) {
		
		GovioServiceInstanceEntity instance = this.serviceInstanceRepo.findById(serviceInstance)
				.orElseThrow( () -> new SemanticValidationException(this.sinstanceMessages.idNotFound(serviceInstance)));
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER,  GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_SYSADMIN) ;
		
		BaseMessage message = BaseMessage.builder()
				.dueDate(govioNewMessage.getDueDate().toLocalDateTime())
				.email(govioNewMessage.getEmail())
				.scheduledExpeditionDate(govioNewMessage.getScheduledExpeditionDate().toLocalDateTime())
				.taxcode(govioNewMessage.getTaxcode())
				.build();
		if(govioNewMessage.getPayment() != null) {
			message.setInvalidAfterDueDate(govioNewMessage.getPayment().getInvalidAfterDueDate());
			message.setNoticeNumber(govioNewMessage.getPayment().getNoticeNumber());
			message.setPayee(govioNewMessage.getPayment().getPayeeTaxcode());
			message.setAmount(govioNewMessage.getPayment().getAmount());
		}
		Map<String, String> placeholderValues = new HashMap<>();
		if(govioNewMessage.getPlaceholders() != null)
			for(GovioNewMessageAllOfPlaceholders p : govioNewMessage.getPlaceholders()) {
				placeholderValues.put(p.getName(), p.getValue());
			}
		GovioMessageEntity messageEntity = this.messageService.newMessage(SecurityService.getPrincipal().getId(), instance.getId(), message, placeholderValues);
		
		return ResponseEntity.ok(this.messageAssembler.toModel(messageEntity));
	}

	
}

















