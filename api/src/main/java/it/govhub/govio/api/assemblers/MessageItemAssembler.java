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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.GovioMessageItem;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.web.FileController;

@Component
public class MessageItemAssembler extends RepresentationModelAssemblerSupport<GovioMessageEntity, GovioMessageItem> {
	
	Logger log = LoggerFactory.getLogger(MessageItemAssembler.class);

	public MessageItemAssembler() {
		super(FileController.class, GovioMessageItem.class);
	}

	@Override
	public GovioMessageItem toModel(GovioMessageEntity src) {
		log.debug("Assembling Entity [GovioMessage] to model...");

		GovioMessageItem ret = instantiateModel(src);
		ret.setId(src.getId());
		ret.setCreationDate(src.getCreationDate());
		ret.setAppioMessageId(src.getAppioMessageId());
		ret.setExpeditionDate(src.getExpeditionDate());
		ret.setStatus(src.getStatus());
		return ret;
	}

}
