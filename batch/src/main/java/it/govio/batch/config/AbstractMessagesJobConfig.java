package it.govio.batch.config;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.step.GovioMessageAbstractProcessor;


@EnableBatchProcessing
public abstract class AbstractMessagesJobConfig  {

	@Autowired
	JobBuilderFactory jobs;

	@Autowired
	StepBuilderFactory steps;
	
	@Autowired
	GovioMessagesRepository govioMessagesRepository;
	
	@Autowired
	EntityManager entityManager;
	
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
	
	protected ItemReader<GovioMessageEntity> expiredScheduledDateMessageCursor(Status[] statuses) {
		
        JpaCursorItemReader<GovioMessageEntity> itemReader = new JpaCursorItemReader<>();
        itemReader.setQueryString("SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE msg.status IN :statuses AND msg.scheduledExpeditionDate < :now");
        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        itemReader.setSaveState(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statuses", Arrays.asList(statuses));
        parameters.put("now", LocalDateTime.now());
        itemReader.setParameterValues(parameters);
        return itemReader;
    }

}
