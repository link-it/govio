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
package it.govio.batch.config;


import javax.persistence.EntityManager;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.step.GovioMessageAbstractProcessor;


@EnableBatchProcessing
public abstract class AbstractMessagesJobConfig  {

	@Autowired
	protected JobBuilderFactory jobs;

	@Autowired
	protected StepBuilderFactory steps;
	
	@Autowired
	protected GovioMessagesRepository govioMessagesRepository;
	
	@Autowired
	protected EntityManager entityManager;
	
	protected TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor("spring_batch_msgsender");
	}
	
	protected AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity> asyncProcessor(GovioMessageAbstractProcessor itemProcessor) {
	    AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity> asyncItemProcessor = new AsyncItemProcessor<>();
	    asyncItemProcessor.setTaskExecutor(taskExecutor());
	    asyncItemProcessor.setDelegate(itemProcessor);
	    return asyncItemProcessor;
	}
	
	protected AsyncItemWriter<GovioMessageEntity> asyncMessageWriter(){
		AsyncItemWriter<GovioMessageEntity> asyncItemWriter = new AsyncItemWriter<>();
	    asyncItemWriter.setDelegate(messageWriter());
	    return asyncItemWriter;
	}
	
	protected RepositoryItemWriter<GovioMessageEntity> messageWriter() {
        final RepositoryItemWriter<GovioMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(govioMessagesRepository);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }
	
}
