package it.govio.batch.step;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.temporal.ChronoUnit;


import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.entity.GovioTemplatePlaceholderEntity;
import it.govio.batch.entity.GovioPlaceholderEntity;
import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileMessageEntity;

import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioFileMessagesRepository;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioTemplatesRepository;
import it.govio.batch.repository.GovioTemplatePlaceholdersRepository;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessProcessor {
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	@Autowired
	private GovioTemplatesRepository govioTemplatesRepository;
	@Autowired
	private GovioTemplatePlaceholdersRepository govioTemplatePlaceholdersRepository;
	@Autowired
	private GovioFileMessagesRepository govioFileMessagesRepository;
	@Autowired
	private GovioFilesRepository govioFilesRepository;
	
	public String process(GovioMessageEntity item) throws Exception {
		if (!validation(item)) return "FAIL";
		String markdown = application(item);
		return save(item, markdown);
	}

	String save(GovioMessageEntity item,String markdown) {
		item.setMarkdown(markdown);
		item.setStatus(Status.SCHEDULED);
		item.setCreationDate(LocalDateTime.now());
		govioMessagesRepository.save(item);
		
		GovioFileMessageEntity govioFileMessageEntity = new GovioFileMessageEntity();
		// l'id govio va preso dalla entity che viene passata come parametro
		govioFileMessageEntity.setGovioMessage(item);
		govioFileMessagesRepository.save(govioFileMessageEntity);
		
		GovioFileEntity govioFileEntity = new GovioFileEntity();
	//	govioFilesRepository.save(govioFileEntity);
		
		return (item.getMarkdown());
	}

	
	String application(GovioMessageEntity item) {
		 // prende il template dalla service instance 
		 GovioServiceInstanceEntity serviceInstance = item.getGovioServiceInstance();
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
			 valuesMap.put("taxcode",item.getTaxcode());
			 break;
		  case "expeditionDate":
			  // tronca la data ai minuti
			  LocalDateTime date = item.getExpeditionDate().truncatedTo(ChronoUnit.MINUTES);
			  String[] parts = date.toString().split("T");
			 valuesMap.put("expeditionDate.date", parts[0]);
			 valuesMap.put("expeditionDate.time", parts[1]);
			 break;
		  default:
		}
			 // validazione in relazione al tipo
			 placeholderEntity.getType();
		 }
		 // effettuo il replace dei valori nel template
		 String templateString = messageTemplateEntity.getMessage_body();
		 StringSubstitutor sub = new StringSubstitutor(valuesMap);
		 String resolvedString = sub.replace(templateString);
		 return resolvedString;
	}

	boolean validation(GovioMessageEntity item) {
		// [A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]
		String fiscal_code = item.getTaxcode();
		for (int i = 0 ; i < 6 ; i++) {
		    if (fiscal_code.charAt(i) < 'A' && fiscal_code.charAt(i) >'Z') return false;
		}
		item.getExpeditionDate();
		return true;
	}
}