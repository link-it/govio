package it.govio.batch.step;

import java.util.Hashtable;
import java.util.List;

import org.springframework.batch.item.ItemWriter;

import it.govio.batch.config.SendMessagesJobConfig;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.repository.GovioMessagesRepository;


/**
 * Message Writer che salva su RAM i riferimenti agli elementi del chunk corrente.
 * 
 * La scrittura sul database potrebbe fallire, e una riesecuzione del job può discernere se un messaggio
 * è stato già inviato o meno. 
 * 
 * @author Francesco Scarlato
 *
 */
public class BackupSendMessageWriter implements ItemWriter<GovioMessageEntity> {
	
	GovioMessagesRepository messageRepo;

	@Override
	public void write(List<? extends GovioMessageEntity> items) throws Exception {
		
		Hashtable<Long, GovioMessageEntity> chunk = new Hashtable<>(items.size());
		for (var i : items) {
			chunk.put(i.getId(), i);
		}
		SendMessagesJobConfig.temporaryChunkMessageStore = chunk;
		
		this.messageRepo.saveAll(items);
	}
	
	public void setMessageRepo(GovioMessagesRepository repo) {
		this.messageRepo = repo;
	}
	
}