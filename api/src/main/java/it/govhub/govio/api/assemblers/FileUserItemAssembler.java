/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
