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
package it.govio.batch.step;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.entity.GovioTemplatePlaceholderEntity;
import it.govio.template.Placeholder;
import it.govio.template.Template;

/**
 *  Per ogni file dei govioFileEntities crea un ExecutionContext con dentro le informazioni del GovioFile,
 *  inoltre inizializza il template per quel file.
 *
 */
public class GovioFilePartitioner implements Partitioner {

	List<GovioFileEntity> govioFileEntities;
	private Logger logger = LoggerFactory.getLogger(GovioFilePartitioner.class);
	
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		logger.debug("Partitioning [{}] Govio Files with a grid of size [{}]", govioFileEntities.size(), gridSize);
		
		Map<String, ExecutionContext> result = new HashMap<> (gridSize);
		for (GovioFileEntity file : govioFileEntities){
			// Devo recuperare la lista dei placeholder previsti
			ExecutionContext ex = new ExecutionContext();
			ex.putString("location", file.getLocation());
			ex.putLong("id", file.getId());
			ex.putLong("govhubUserId", file.getGovhubUserId());

			GovioTemplateEntity govioTemplate = file.getGovioServiceInstance().getGovioTemplate();
			
			List<Placeholder> placeholders = new ArrayList<>();
			for(GovioTemplatePlaceholderEntity e : govioTemplate.getGovioTemplatePlaceholders()) {
				Placeholder p =  Placeholder.builder()
						.mandatory(e.isMandatory())
						.name(e.getGovioPlaceholder().getName())
						.pattern(e.getGovioPlaceholder().getPattern())
						.position(e.getPosition())
						.type(Placeholder.Type.valueOf(e.getGovioPlaceholder().getType().name()))
						.build();
				placeholders.add(p);
			}
			
			Template template = Template.builder()
					.hasDueDate(govioTemplate.getHasDueDate())
					.hasPayment(govioTemplate.getHasPayment())
					.messageBody(govioTemplate.getMessageBody())
					.placeholders(placeholders)
					.subject(govioTemplate.getSubject())
					.build();
			
			ex.put("template", template);
			ex.putLong("serviceInstance", file.getGovioServiceInstance().getId());
			result.put("F"+file.getId(), ex);
			logger.debug("ExecutionContext {} aggiunto [id:{}, location:{}, template:{}, serviceInstance: {}]", 
					"F"+file.getId(), 
					ex.getLong("id"), 
					ex.getString("location"), 
					govioTemplate.getId(),
					ex.getLong("serviceInstance"));
		}
		return result;
	}

	public void setGovioFileEntities(List<GovioFileEntity> govioFileEntities) {
		this.govioFileEntities = govioFileEntities;
	}
}
