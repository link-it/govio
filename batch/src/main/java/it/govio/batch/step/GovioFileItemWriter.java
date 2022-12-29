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
import it.govio.batch.repository.GovioMessagesRepository;

public class GovioFileItemWriter implements ItemWriter<GovioFileMessageEntity> {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	GovioMessagesRepository repository;
	@Autowired
	GovioFileMessagesRepository repositoryRelation;
	
	long govioFileId;
	long govioServiceInstanceId;
	
	@Override
	public void write(List<? extends GovioFileMessageEntity> items) throws Exception {
		GovioFileEntity govioFileReference = em.getReference(GovioFileEntity.class, govioFileId);
		GovioServiceInstanceEntity govioServiceInstanceReference = em.getReference(GovioServiceInstanceEntity.class, govioServiceInstanceId);
		for(GovioFileMessageEntity item : items) {
			item.setGovioFile(govioFileReference);
			if(item.getGovioMessage() != null) {
				item.getGovioMessage().setGovioServiceInstance(govioServiceInstanceReference);
				item.getGovioMessage().setGovioFileMessage(item);
			}
			repository.save(item.getGovioMessage());
			repositoryRelation.save(item);
		}
	}

	public void setGovioFileId(long govioFileId) {
		this.govioFileId = govioFileId;
	}
	
	public void setGovioServiceInstanceId(long govioServiceInstanceId) {
		this.govioServiceInstanceId = govioServiceInstanceId;
	}

}
