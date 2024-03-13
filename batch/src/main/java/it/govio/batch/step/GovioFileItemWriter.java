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
package it.govio.batch.step;

import org.slf4j.Logger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFileMessagesRepository;

public class GovioFileItemWriter implements ItemWriter<GovioFileMessageEntity> {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	GovioFileMessagesRepository repository;
	
	long govioFileId;
	long govioServiceInstanceId;
	long govhubUserId;
	
	Logger log = LoggerFactory.getLogger(GovioFileItemWriter.class);
	
	@Override
	public void write(List<? extends GovioFileMessageEntity> items) throws Exception {
		log.debug("Writing {} messages for File Id [{}], ServiceInstance [{}] with User uploader [{}]", items.size(), govioFileId, govioServiceInstanceId, govhubUserId);
		
		GovioFileEntity govioFileReference = em.getReference(GovioFileEntity.class, govioFileId);
		GovioServiceInstanceEntity govioServiceInstanceReference = em.getReference(GovioServiceInstanceEntity.class, govioServiceInstanceId);
		for(GovioFileMessageEntity item : items) {
			item.setGovioFile(govioFileReference);
			if(item.getGovioMessage() != null) {
				item.getGovioMessage().setGovioServiceInstance(govioServiceInstanceReference);
				item.getGovioMessage().setGovhubUserId(govhubUserId);
			}
			try {
				repository.save(item);
			} catch (Exception e) {
				log.error("Exception while saving GovioFIleMessageEntity: {}", e.getMessage());
				throw (e);
			}
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
