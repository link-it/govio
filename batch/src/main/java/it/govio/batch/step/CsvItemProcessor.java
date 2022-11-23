package it.govio.batch.step;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioPlaceholderEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.entity.GovioTemplatePlaceholderEntity;
import it.govio.batch.exception.TemplateValidationException;
import it.govio.batch.repository.GovioFileMessagesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioTemplatePlaceholdersRepository;
import it.govio.batch.repository.GovioTemplatesRepository;
import it.govio.batch.step.beans.CsvItem;
import it.govio.batch.step.beans.GovioMessage;

@Component
public class CsvItemProcessor implements ItemProcessor<CsvItem, GovioMessage> {
	/**
	 * Processor che prende una item del CSV e lo trasforma 
	 * in un GovioMessageEntity in base al template associato al 
	 * file
	 */
	public GovioMessage process(CsvItem item) {

		GovioMessage govioMessage = new GovioMessage();
		GovioFileMessageEntity govioFileMessageEntity = GovioFileMessageEntity.builder().line_number(item.getRowNumber()).build();

		try { 
			validation(item);
		} catch (TemplateValidationException e) {
			govioFileMessageEntity.setError(e.getMessage());
			return GovioMessage.builder().govioFileMessageEntity(govioFileMessageEntity ).build(); //TODO
		}

		// Recuperare il template

		// Applicare il template ai valori dell'item

		// Costruire il message entity
		GovioMessageEntity messageEntity = GovioMessageEntity.builder().build(); //TODO

		govioMessage.setGovioFileMessageEntity(govioFileMessageEntity);
		govioMessage.setGovioMessageEntity(messageEntity);
		return govioMessage;
	}

	String buildGovioMessageMarkdown(GovioServiceInstanceEntity serviceInstance, CsvItem item) {
		// prende il template dalla service instance 
		GovioTemplateEntity messageTemplateEntity = govioTemplatesRepository.findByGovioServiceInstance(serviceInstance);
		// prende il templateplaceholder tramite il template
		long idTemplate = messageTemplateEntity.getId();
		List<GovioTemplatePlaceholderEntity> govioTemplatePlaceholderEntity = govioTemplatePlaceholdersRepository.findByGovioTemplateId(idTemplate);

		Map<String, String> valuesMap = new HashMap<String, String>();

		// scorre la lista di templateplaceholder, prendendo ogni placeholder dalla placeholderEntity
		for (GovioTemplatePlaceholderEntity elem : govioTemplatePlaceholderEntity) {
			// trovare un nome migliore a placeholderEntity
			GovioPlaceholderEntity placeholderEntity = elem.getGovioPlaceholder();
			// trovare il modo di farlo qui dentro al for
			switch(placeholderEntity.getName()) {
			case "taxcode":
				valuesMap.put("taxcode", item.getTaxcode());
				break;
			case "expeditionDate":
				// tronca la data ai minuti
				LocalDateTime date = item.getScheduledDate().truncatedTo(ChronoUnit.MINUTES);
				String[] parts = date.toString().split("T");
				valuesMap.put("expeditionDate.date", parts[0]);
				valuesMap.put("expeditionDate.time", parts[1]);
				break;
			default:
			}
		}
		// effettuo il replace dei valori nel template
		String templateString = messageTemplateEntity.getMessageBody();
		StringSubstitutor sub = new StringSubstitutor(valuesMap);
		String resolvedString = sub.replace(templateString);
		return resolvedString;
	}

	void validation(CsvItem item) throws TemplateValidationException {
		// Qui valido che:
		// - I campi obbligatori ci siano
		// - Se un campo ha un pattern impostato sia rispettato
		// - Se un campo abbia un valore compatibile con il tipo
		// TODO
	}
}