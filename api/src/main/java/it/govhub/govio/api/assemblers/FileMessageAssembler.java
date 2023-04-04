/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govio.api.beans.FileMessage;
import it.govhub.govio.api.beans.FileMessageStatusEnum;
import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.web.FileController;

@Component
public class FileMessageAssembler extends RepresentationModelAssemblerSupport<GovioFileMessageEntity, FileMessage> {

	Logger log = LoggerFactory.getLogger(FileMessageAssembler.class);
	
	@Autowired
	MessageItemAssembler msgItemAssembler;

	public FileMessageAssembler() {
		super(FileController.class, FileMessage.class);
	}

	@Override
	public FileMessage toModel(GovioFileMessageEntity src) {
		log.debug("Assembling Entity [GovioFileMessage] to model...");

		FileMessage ret = instantiateModel(src);

		FileMessageStatusEnum status = src.getGovioMessage() == null ? FileMessageStatusEnum.ERROR
				: FileMessageStatusEnum.ACQUIRED;

		BeanUtils.copyProperties(src, ret);

		if (status == FileMessageStatusEnum.ACQUIRED) {
			ret.status(status).message(this.msgItemAssembler.toModel(src.getGovioMessage()));
		}

		return ret;
	}

}
