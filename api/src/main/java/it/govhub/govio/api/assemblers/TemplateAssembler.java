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

import it.govhub.govio.api.beans.GovioTemplate;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.web.TemplateController;

@Component
public class TemplateAssembler  extends RepresentationModelAssemblerSupport<GovioTemplateEntity, GovioTemplate>{

	Logger log = LoggerFactory.getLogger(TemplateAssembler.class);
	
	public TemplateAssembler() {
		super(TemplateController.class, GovioTemplate.class);
	}

	@Override
	public GovioTemplate toModel(GovioTemplateEntity entity) {
		log.debug("Assembling Entity [GovioTemplate] to model...");

		GovioTemplate ret = new GovioTemplate();
		BeanUtils.copyProperties(entity, ret);

		ret.add(
				linkTo(
					methodOn(TemplateController.class)	.readTemplate(entity.getId()))
				.withSelfRel()
			).add(
				linkTo(
						methodOn(TemplateController.class)	.listTemplatePlaceholders(entity.getId(), null))
				.withSelfRel());
				
		return ret;
	}

	public GovioTemplateEntity toEntity(GovioTemplate model) {
		log.debug("Converting Model [GovioTemplate] to entity...");
		
		GovioTemplateEntity ret = new GovioTemplateEntity();
		BeanUtils.copyProperties(model, ret);
		
		return ret;
	}

}
