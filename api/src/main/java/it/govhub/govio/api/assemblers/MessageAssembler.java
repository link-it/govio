package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.HalModelBuilder;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

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
}
