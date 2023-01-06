package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.beans.GovioMessagePaymentItem;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.web.FileController;
import it.govhub.govio.api.web.MessageController;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAuthItemAssembler;
import it.govhub.govregistry.readops.api.assemblers.ServiceAuthItemAssembler;

@Component
public class MessageAssembler extends RepresentationModelAssemblerSupport<GovioMessageEntity, GovioMessage> {

	@Autowired
	FileUserItemAssembler userItemAssembler;
	
	@Autowired
	OrganizationAuthItemAssembler orgItemAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceItemAssembler;
	
	public MessageAssembler() {
		super(FileController.class, GovioMessage.class);
	}

	@Override
	public GovioMessage toModel(GovioMessageEntity src) {
		GovioMessage ret = instantiateModel(src);

		BeanUtils.copyProperties(src, ret);
		
		GovioMessagePaymentItem payment = new GovioMessagePaymentItem();
		payment.amount(src.getAmount())
			.noticeNumber(src.getNoticeNumber())
			.invalidAfterDueDate(src.getInvalidAfterDueDate())
			.payeeTaxcode(src.getPayeeTaxcode());
		
		ret.user(this.userItemAssembler.toModel(src.getSender()))
			.organization(this.orgItemAssembler.toModel(src.getGovioServiceInstance().getOrganization()))
			.service(this.serviceItemAssembler.toModel(src.getGovioServiceInstance().getService().getGovhubService()))
			.payment(payment);
		
		ret.add(linkTo(methodOn(MessageController.class).readMessage(src.getId())).withSelfRel());
		
		return ret;
	}
}
