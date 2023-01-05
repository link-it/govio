package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioMessage;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.web.FileController;

@Component
public class MessageAssembler extends RepresentationModelAssemblerSupport<GovioMessageEntity, GovioMessage> {

	public MessageAssembler() {
		super(FileController.class, GovioMessage.class);
	}

	@Override
	public GovioMessage toModel(GovioMessageEntity src) {
		GovioMessage ret = instantiateModel(src);
		BeanUtils.copyProperties(src, ret);
		ret.add(linkTo(methodOn(FileController.class).readMessage(src.getId())).withSelfRel());

		return ret;
	}
}
