package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

		ret.add(linkTo(
				methodOn(MessageController.class)
					.readMessage(src.getId())
				).withSelfRel())
			.add(linkTo(
				methodOn(ServiceInstanceController.class)
					.readServiceInstance(src.getGovioServiceInstance().getId()))
				.withRel("service-instance"))
		.add(linkTo(
				methodOn(ReadUserController.class)
					.readUser(src.getSender().getId()))
				.withRel("sender"));
		
		return ret;
	}
}
