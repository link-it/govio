package it.govhub.govio.api.services;

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
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.messages.MessageMessages;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;

@Service
public class MessageService {
	
	@Autowired
	GovioMessageRepository messageRepo;
	
	@Autowired
	MessageAssembler messageAssembler;
	
	@Autowired
	MessageMessages messageMessages;

	@Transactional
	public GovioMessage readMessage(Long id) {
		
		GovioMessageEntity message = this.messageRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(this.messageMessages.idNotFound(id)));

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

}














