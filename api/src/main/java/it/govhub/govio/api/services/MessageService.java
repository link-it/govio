package it.govhub.govio.api.services;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govio.api.assemblers.MessageAssembler;
import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessageList;
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.messages.MessageMessages;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.security.services.SecurityService;
import it.govio.template.BaseMessage;
import it.govio.template.BasicTemplateApplier;
import it.govio.template.Placeholder;
import it.govio.template.Template;
import it.govio.template.TemplateApplierFactory;

@Service
public class MessageService {
	
	@Autowired
	GovioMessageRepository messageRepo;
	
	@Autowired
	GovioServiceInstanceRepository serviceInstanceRepo;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	MessageAssembler messageAssembler;
	
	@Autowired
	MessageMessages messageMessages;
	
	@Autowired
	SecurityService authService;

	@Transactional
	public GovioMessage readMessage(Long id) {
		
		GovioMessageEntity message = this.messageRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(this.messageMessages.idNotFound(id)));
		
		GovioServiceInstanceEntity instance = message.getGovioServiceInstance();
		
		this.authService.hasAnyOrganizationAuthority(instance.getOrganization().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN);
		this.authService.hasAnyServiceAuthority(instance.getService().getId(), GovioRoles.GOVIO_SENDER, GovioRoles.GOVIO_VIEWER, GovioRoles.GOVIO_SYSADMIN) ;

		return this.messageAssembler.toModel(message);
	}


	
	@Transactional
	public GovioMessageList listMessages(Specification<GovioMessageEntity> spec, LimitOffsetPageRequest pageRequest) {
		Page<GovioMessageEntity> messages = this.messageRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		GovioMessageList ret = ListaUtils.buildPaginatedList(messages, pageRequest.limit, curRequest, new GovioMessageList());
		
		for (var message: messages) {
			ret.addItemsItem(this.messageAssembler.toModel(message));
		}
		return ret;
	}
	
	
	@Transactional
	public GovioMessageEntity newMessage(long senderId, long serviceInstanceId, BaseMessage message, Map<String, String> placeholderValues) {
		
		GovioServiceInstanceEntity serviceInstance = serviceInstanceRepo.getReferenceById(serviceInstanceId);
		
		GovioTemplateEntity templateEntity = serviceInstance.getTemplate();
		
		List<Placeholder> placeholders = new ArrayList<> ();
		for(GovioTemplatePlaceholderEntity placeholderEntity : templateEntity.getGovioTemplatePlaceholders()) {
			Placeholder p = Placeholder.builder()
					.mandatory(placeholderEntity.isMandatory())
					.name(placeholderEntity.getGovioPlaceholder().getName())
					.pattern(placeholderEntity.getGovioPlaceholder().getPattern())
					.position(placeholderEntity.getPosition())
					.type(Placeholder.Type.valueOf(placeholderEntity.getGovioPlaceholder().getType().toString()))
					.build();
			placeholders.add(p);
		}
		Template template = Template
				.builder()
				.hasDueDate(templateEntity.getHasDueDate())
				.hasPayment(templateEntity.getHasPayment())
				.messageBody(templateEntity.getMessageBody())
				.subject(templateEntity.getSubject())
				.placeholders(placeholders)
				.build();
		
		BasicTemplateApplier templateApplier = TemplateApplierFactory.buildBasicTemplateApplier(template);
		String subject = templateApplier.getSubject(message, placeholderValues);
		String markdown = templateApplier.getMarkdown(message, placeholderValues);
		
		OffsetDateTime now = OffsetDateTime.now();
		GovioMessageEntity newMessage = GovioMessageEntity.builder()
				.amount(message.getAmount())
				.creationDate(now)
				.dueDate(message.getDueDate().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime())
				.email(message.getEmail())
				.govioServiceInstance(serviceInstance)
				.invalidAfterDueDate(message.getInvalidAfterDueDate())
				.lastUpdateStatus(now)
				.markdown(markdown)
				.noticeNumber(message.getNoticeNumber())
				.payeeTaxcode(message.getPayee())
				.scheduledExpeditionDate(message.getScheduledExpeditionDate().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime())
				.sender(em.getReference(UserEntity.class, senderId))
				.status(GovioMessageEntity.Status.SCHEDULED)
				.subject(subject)
				.taxcode(message.getTaxcode())
				.build();
		
		return messageRepo.save(newMessage);
	}
	
}


