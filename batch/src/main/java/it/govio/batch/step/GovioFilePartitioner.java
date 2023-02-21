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
		Map<String, ExecutionContext> result = new HashMap<> (gridSize);
		for (GovioFileEntity file : govioFileEntities){
			// Devo recuperare la lista dei placeholder previsti
			ExecutionContext ex = new ExecutionContext();
			ex.putString("location", file.getLocation());
			ex.putLong("id", file.getId());
			ex.putLong("govhubUserId", file.getGovhubUserId());
			// Se il service_instance non presenta un template, recupero il default dal service
			GovioTemplateEntity govioTemplate = null;
			if (file.getGovioServiceInstance().getGovioTemplate() == null) 
				govioTemplate = file.getGovioServiceInstance().getGovioService().getGovioTemplate();
			else 
				govioTemplate = file.getGovioServiceInstance().getGovioTemplate();
			
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
