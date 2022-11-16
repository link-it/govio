package it.govio.batch.config;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.step.GetProfileProcessor;
import it.govio.batch.step.GovioMessageAbstractProcessor;
import it.govio.batch.step.NewMessageProcessor;


@Configuration
@EnableBatchProcessing
public class BatchConfig  {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	private GetProfileProcessor getProfileProcessor;

	@Autowired
	private NewMessageProcessor newMessageProcessor;
	
	@Autowired
	private NewMessageProcessor getMessageProcessor;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor("spring_batch_msgsender");
	}
	
	@Bean(name = "JobSpedizioneMessaggiIO")
	public Job spedizioneMessaggiIO(){
		return jobs.get("JobSpedizioneMessaggiIO")
				.incrementer(new RunIdIncrementer())
				.start(getProfileStep())
				.next(newMessageStep())
				.build();
	}
	
	@Bean(name = "JobVerificaMessaggiIO")
	public Job verificaMessaggiIO(){
		return jobs.get("JobVerificaMessaggiIO")
				.incrementer(new RunIdIncrementer())
				.start(getMessageStep())
				.build();
	}
	
	public Step getProfileStep(){
		Status[] statuses = {Status.SCHEDULED};
		return steps.get("getProfileStep")
				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
				.reader(expiredScheduledDateMessageCursor(statuses))
				.processor(asyncProcessor(this.getProfileProcessor))
				.writer(asyncMessageWriter())
				.faultTolerant()
				.skip(Throwable.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}
	
	public Step newMessageStep(){
		Status[] statuses = {Status.RECIPIENT_ALLOWED};
		return steps.get("newMessageStep")
				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(1)
				.reader(expiredScheduledDateMessageCursor(statuses))
				.processor(asyncProcessor(this.newMessageProcessor))
				.writer(asyncMessageWriter())
				.faultTolerant()
				.retry(Throwable.class)
				.retryLimit(5)
				.build();
	}
	
	public Step getMessageStep(){
		
		Status[] statuses = {Status.SENT, Status.THROTTLED, Status.ACCEPTED};
		return steps.get("getMessaggeStep")
		.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
		.reader(expiredScheduledDateMessageCursor(statuses))
		.processor(asyncProcessor(this.getMessageProcessor))
		.writer(asyncMessageWriter())
		.faultTolerant()
		.skip(Throwable.class)
		.skipLimit(Integer.MAX_VALUE)
		.build();
	}
	
	private AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity> asyncProcessor(GovioMessageAbstractProcessor itemProcessor) {
	    AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity> asyncItemProcessor = new AsyncItemProcessor<>();
	    asyncItemProcessor.setTaskExecutor(taskExecutor());
	    asyncItemProcessor.setDelegate(itemProcessor);
	    return asyncItemProcessor;
	}
	
	private AsyncItemWriter<GovioMessageEntity> asyncMessageWriter(){
		AsyncItemWriter<GovioMessageEntity> asyncItemWriter = new AsyncItemWriter<>();
	    asyncItemWriter.setDelegate(messageWriter());
	    return asyncItemWriter;
	}
	
    private RepositoryItemWriter<GovioMessageEntity> messageWriter() {
        final RepositoryItemWriter<GovioMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(govioMessagesRepository);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }
	
    public ItemReader<GovioMessageEntity> expiredScheduledDateMessageCursor(Status[] statuses) {
        JpaCursorItemReader<GovioMessageEntity> itemReader = new JpaCursorItemReader<>();
        itemReader.setQueryString("SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE msg.status IN :statuses AND msg.scheduledExpeditionDate < :now");
        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        itemReader.setSaveState(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statuses", statuses);
        parameters.put("now", LocalDateTime.now());
        itemReader.setParameterValues(parameters);
        return itemReader;
    }


}
