package it.govhub.govio.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.FileUserItem;
import it.govhub.govregistry.commons.api.spec.UserApi;
import it.govhub.govregistry.commons.entity.UserEntity;

@Component
public class FileUserItemAssembler extends RepresentationModelAssemblerSupport<UserEntity, FileUserItem> {
	

	public FileUserItemAssembler() {
		super(UserApi.class, FileUserItem.class);
	}

	@Override
	public FileUserItem  toModel(UserEntity src) {
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
