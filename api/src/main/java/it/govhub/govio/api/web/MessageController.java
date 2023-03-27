package it.govhub.govio.api.web;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.assemblers.MessageAssembler;
import it.govhub.govio.api.beans.EmbedMessageEnum;
import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessageList;
import it.govhub.govio.api.beans.GovioNewMessage;
import it.govhub.govio.api.beans.MessageOrdering;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.messages.MessageMessages;
import it.govhub.govio.api.messages.ServiceInstanceMessages;
import it.govhub.govio.api.repository.MessageFilters;
import it.govhub.govio.api.repository.MessageRepository;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.services.MessageService;
import it.govhub.govio.api.spec.MessageApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.security.services.SecurityService;
import it.govio.template.BaseMessage;
import it.govio.template.exception.TemplateValidationException;

@V1RestController
public class MessageController implements MessageApi {
	
	@Autowired
	MessageService messageService;
	
	@Autowired
	MessageAssembler messageAssembler;

	@Autowired
	ServiceInstanceRepository serviceInstanceRepo;
	
	@Autowired
	MessageRepository messageRepo;
	
	@Autowired
	ServiceInstanceMessages sinstanceMessages;
	
	@Autowired
	MessageMessages messageMessages;
	
	@Autowired
	SecurityService authService;
	
	Logger log = LoggerFactory.getLogger(MessageController.class);
	
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
			String serviceQ,
			String organizationQ,
			Integer limit,
			Long offset,
			List<EmbedMessageEnum> embeds) {
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, MessageFilters.sort(orderBy, sortDirection));
		
		// Pesco servizi e autorizzazioni che l'utente può leggere
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER);
		Set<Long> serviceIds = this.authService.listAuthorizedServices(GovioRoles.GOVIO_SYSADMIN, GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER);
		
		Specification<GovioMessageEntity> spec = MessageFilters.empty();
		
		if (orgIds != null) {
			spec = spec.and(MessageFilters.byOrganizationIds(orgIds));
		}
		if (serviceIds != null) {
			spec = spec.and(MessageFilters.byServiceIds(serviceIds));
		}
		if (scheduledExpeditionDateFrom != null) {
			spec = spec.and(MessageFilters.fromScheduledExpeditionDate(scheduledExpeditionDateFrom));
		}
		if (scheduledExpeditionDateTo != null) {
			spec = spec.and(MessageFilters.toScheduledExpeditionDate(scheduledExpeditionDateTo));
		}
		if (expeditionDateFrom != null) {
			spec = spec.and(MessageFilters.fromExpeditionDate(expeditionDateFrom));
		}
		if (expeditionDateTo != null) {
			spec = spec.and(MessageFilters.toExpeditionDate(expeditionDateTo));
		}
		if (taxCode != null) {
			spec = spec.and(MessageFilters.likeTaxcode(taxCode));
		}
		if (serviceId != null) {
			spec = spec.and(MessageFilters.byServiceId(serviceId));
		}
		if (organizationId != null) {
			spec = spec.and(MessageFilters.byOrganizationId(organizationId));
		}
		if (serviceQ != null) {
			spec = spec.and(MessageFilters.likeServiceName(serviceQ));
		}
		if (organizationQ != null) {
			spec = spec.and(MessageFilters.likeOrganizationName(organizationQ).or(MessageFilters.likeOrganizationTaxCode(organizationQ))); 
		}
		
		GovioMessageList ret = this.messageService.listMessages(spec, pageRequest, embeds);
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<GovioMessage> readMessage(Long id) {
		
		GovioMessageEntity message = this.messageRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(this.messageMessages.idNotFound(id)));
		
		GovioServiceInstanceEntity instance = message.getGovioServiceInstance();
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;

		return ResponseEntity.ok(this.messageAssembler.toEmbeddedModel(message));
	}
	
    
	@Transactional
	@Override
	public ResponseEntity<GovioMessage> sendMessage(Long serviceInstance, GovioNewMessage govioNewMessage) {
		
		// Faccio partire la validazione custom per la stringa \u0000
		if (govioNewMessage.getPlaceholders() != null) {
			int i = 0;
			for (var p : govioNewMessage.getPlaceholders()) {
				PostgreSQLUtilities.throwIfContainsNullByte(p.getName(), "placeholders["+i+"].name");
				PostgreSQLUtilities.throwIfContainsNullByte(p.getValue(), "placeholders["+i+"].value");
				i++;
			}
		}
		
		UserEntity principal = SecurityService.getPrincipal();
		
		log.info("Sending new message from user [{}] to service instance [{}]: {} ", principal.getPrincipal(), serviceInstance, govioNewMessage);
		
		GovioServiceInstanceEntity instance = this.serviceInstanceRepo.findById(serviceInstance)
				.orElseThrow( () -> new SemanticValidationException(this.sinstanceMessages.idNotFound(serviceInstance)));
		
    	if (!instance.getEnabled() ) {
    		throw new SemanticValidationException("La service instance ["+instance.getId()+"] è disabilitata.");
    	}
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER,  GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_SYSADMIN) ;
		
		BaseMessage message = BaseMessage.builder()
				.dueDate(govioNewMessage.getDueDate() == null ? null : govioNewMessage.getDueDate().toLocalDateTime())
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
			for(var p : govioNewMessage.getPlaceholders()) {
				placeholderValues.put(p.getName(), p.getValue());
			}
		
		try {
			GovioMessageEntity messageEntity = this.messageService.newMessage(SecurityService.getPrincipal().getId(), instance.getId(), message, placeholderValues);
			return ResponseEntity.ok(this.messageAssembler.toModel(messageEntity));
		} catch (TemplateValidationException e) {
			throw new SemanticValidationException(e.getMessage());
		}
		
	}

	
}

















