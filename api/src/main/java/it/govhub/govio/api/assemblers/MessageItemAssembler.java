package it.govhub.govio.api.assemblers;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioMessageItem;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.web.FileController;

@Component
public class MessageItemAssembler
		extends RepresentationModelAssemblerSupport<GovioMessageEntity, GovioMessageItem> {

	public MessageItemAssembler() {
		super(FileController.class, GovioMessageItem.class);
	}

	@Override
	public GovioMessageItem toModel(GovioMessageEntity src) {
		GovioMessageItem ret = instantiateModel(src);

		ret.setId(src.getId());

		return ret;
	}

}
