package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.FileUserItem;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.spec.UserApi;

@Component
public class FileUserItemAssembler extends RepresentationModelAssemblerSupport<UserEntity, FileUserItem> {

	Logger log = LoggerFactory.getLogger(FileUserItemAssembler.class);

	public FileUserItemAssembler() {
		super(UserApi.class, FileUserItem.class);
	}

	@Override
	public FileUserItem  toModel(UserEntity src) {
		log.debug("Assembling Entity [User] to model...");

		FileUserItem ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(UserApi.class)
				.readUser(src.getId()))
			.withSelfRel()
		) ;
		
		return ret;
	}

}
