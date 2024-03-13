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

import java.util.Hashtable;
import java.util.List;

import org.springframework.batch.item.ItemWriter;

import it.govio.batch.config.SendMessagesJobConfig;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.repository.GovioMessagesRepository;


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