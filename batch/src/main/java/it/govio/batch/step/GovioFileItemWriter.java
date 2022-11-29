package it.govio.batch.step;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.repository.GovioFileMessagesRepository;

public class GovioFileItemWriter implements ItemWriter<GovioFileMessageEntity> {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	GovioFileMessagesRepository repository;
	
	long govioFileEntityId;
	
	@Override
	public void write(List<? extends GovioFileMessageEntity> items) throws Exception {
		for(GovioFileMessageEntity item : items) {
			item.setGovioFile(em.getReference(GovioFileEntity.class, govioFileEntityId));
			repository.save(item);
		}
	}

	public void setGovioFileEntityId(long govioFileEntityId) {
		this.govioFileEntityId = govioFileEntityId;
	}
	
	

}
