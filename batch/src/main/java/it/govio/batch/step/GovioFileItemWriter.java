package it.govio.batch.step;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFileMessagesRepository;

/**
 * Popola i riferimenti agli oggetti di Govio e scrive su database i GofioFileMessageEntity costruiti a partire dalla righe del csv. 
 *
 *
 */
public class GovioFileItemWriter implements ItemWriter<GovioFileMessageEntity> {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	GovioFileMessagesRepository repository;
	
	long govioFileId;
	long govioServiceInstanceId;
	long govhubUserId;
	
	@Override
	public void write(List<? extends GovioFileMessageEntity> items) throws Exception {
		GovioFileEntity govioFileReference = em.getReference(GovioFileEntity.class, govioFileId);
		GovioServiceInstanceEntity govioServiceInstanceReference = em.getReference(GovioServiceInstanceEntity.class, govioServiceInstanceId);
		for(GovioFileMessageEntity item : items) {
			item.setGovioFile(govioFileReference);
			if(item.getGovioMessage() != null) {
				item.getGovioMessage().setGovioServiceInstance(govioServiceInstanceReference);
				item.getGovioMessage().setGovhubUserId(govhubUserId);
			}
			repository.save(item);
		}
	}

	public void setGovioFileId(long govioFileId) {
		this.govioFileId = govioFileId;
	}
	
	public void setGovioServiceInstanceId(long govioServiceInstanceId) {
		this.govioServiceInstanceId = govioServiceInstanceId;
	}

	public void setGovhubUserId(long govhubUserId) {
		this.govhubUserId = govhubUserId;
	}
}
