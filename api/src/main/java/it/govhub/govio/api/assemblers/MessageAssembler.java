package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.govhub.govio.api.beans.EmbedMessageEnum;
import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessagePaymentItem;
import it.govhub.govio.api.beans.GovioMessageStatus;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.web.MessageController;
import it.govhub.govio.api.web.ServiceInstanceController;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;
import it.govhub.govregistry.readops.api.web.ReadUserController;

@Component
public class MessageAssembler extends RepresentationModelAssemblerSupport<GovioMessageEntity, GovioMessage> {

	Logger log = LoggerFactory.getLogger(MessageAssembler.class);
	
	@Autowired
	FileUserItemAssembler userItemAssembler;
	
	@Autowired
	OrganizationAuthItemAssembler orgItemAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceItemAssembler;
	
	@Autowired
	ServiceInstanceAssembler instanceAssembler;
	
	public MessageAssembler() {
		super(MessageController.class, GovioMessage.class);
	}

	@Override
	public GovioMessage toModel(GovioMessageEntity src) {
		log.debug("Assembling Entity [GovioMessage] to model...");

		GovioMessage ret = instantiateModel(src);

		BeanUtils.copyProperties(src, ret);
		ret.setStatus(GovioMessageStatus.valueOf(src.getStatus().toString()));
		
		if(src.getNoticeNumber() != null) {
			GovioMessagePaymentItem payment = new GovioMessagePaymentItem();
			payment.amount(src.getAmount())
				.noticeNumber(src.getNoticeNumber())
				.invalidAfterDueDate(src.getInvalidAfterDueDate())
				.payeeTaxcode(src.getPayeeTaxcode());
			ret.setPayment(payment);
		}
		
		Link linkToServiceInstance = linkTo(
				methodOn(ServiceInstanceController.class)
				.readServiceInstance(src.getGovioServiceInstance().getId()))
			.withRel("service-instance"); 

		ret.add(linkTo(
				methodOn(MessageController.class)
					.readMessage(src.getId())
				).withSelfRel())
			.add(linkToServiceInstance)
		.add(linkTo(
				methodOn(ReadUserController.class)
					.readUser(src.getSender().getId()))
				.withRel("sender"));
		
		return ret;
	}

	public GovioMessage toEmbeddedModel(GovioMessageEntity src, List<EmbedMessageEnum> embed) {
		
		GovioMessage ret = this.toModel(src);
		
		if (!CollectionUtils.isEmpty(embed)) {
			ret.setEmbedded(new HashMap<>());
			
			for(var e : embed) {
				switch(e) {
				case SENDER:
					ret.getEmbedded().put(EmbedMessageEnum.SENDER.getValue(), this.userItemAssembler.toModel(src.getSender()));
					break;
				case SERVICE_INSTANCE:
					ret.getEmbedded().put(EmbedMessageEnum.SERVICE_INSTANCE.getValue(), this.instanceAssembler.toModel(src.getGovioServiceInstance()));
					break;
				default:
					break;
				
				}
			}
		}
		
		return ret;
	}
}







