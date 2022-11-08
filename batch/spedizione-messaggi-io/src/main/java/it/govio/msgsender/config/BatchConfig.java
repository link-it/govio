package it.govio.msgsender.config;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.govio.msgsender.repository.GovioMessagesRepository;
import it.govio.msgsender.step.GetProfileProcessor;
import it.govio.msgsender.step.NewMessageProcessor;


@Configuration
@EnableBatchProcessing
public class BatchConfig  {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	private GetProfileProcessor getProfileProcessor;

	@Autowired
	private NewMessageProcessor newMessageProcessor;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor("spring_batch_msgsender");
	}
	
	public Step getProfileStep(){
		return steps.get("getProfileStep")
//				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
//				.reader(expiredScheduledDateMessageCursor(Status.SCHEDULED))
//				.processor(asyncProcessor(this.getProfileProcessor))
//				.writer(asyncMessageWriter())
				.<GovioMessageEntity, GovioMessageEntity>chunk(10)
				.reader(expiredScheduledDateMessageReader(Status.SCHEDULED))
				.processor(this.getProfileProcessor)
				.writer(messageWriter())
				.build();
	}
	
//	private AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity> asyncProcessor(ItemProcessor<GovioMessageEntity, GovioMessageEntity> itemProcessor) {
//		    AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity> asyncItemProcessor = new AsyncItemProcessor<GovioMessageEntity, GovioMessageEntity>();
//		    asyncItemProcessor.setTaskExecutor(taskExecutor());
//		    asyncItemProcessor.setDelegate(itemProcessor);
//		    return asyncItemProcessor;
//	}

	public Step newMessageStep(){
		return steps.get("newMessageStep")
//				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
//				.reader(expiredScheduledDateMessageCursor(Status.RECIPIENT_ALLOWED))
//				.processor(asyncProcessor(this.newMessageProcessor))
//				.writer(asyncMessageWriter())
				.<GovioMessageEntity, GovioMessageEntity>chunk(10)
				.reader(expiredScheduledDateMessageCursor(Status.RECIPIENT_ALLOWED))
				.processor(this.newMessageProcessor)
				.writer(messageWriter())
				.build();
	}
	
//	private AsyncItemWriter<GovioMessageEntity> asyncMessageWriter(){
//		AsyncItemWriter<GovioMessageEntity> asyncItemWriter = new AsyncItemWriter<GovioMessageEntity>();
//	    asyncItemWriter.setDelegate(messageWriter());
//	    return asyncItemWriter;
//	}
	
    private RepositoryItemWriter<GovioMessageEntity> messageWriter() {
        final RepositoryItemWriter<GovioMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(govioMessagesRepository);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }
	
    private RepositoryItemReader<GovioMessageEntity> expiredScheduledDateMessageReader(Status status) {
        final RepositoryItemReader<GovioMessageEntity> repositoryItemReader = new RepositoryItemReader<>();
        repositoryItemReader.setRepository(govioMessagesRepository);
        repositoryItemReader.setMethodName("findAllByStatusAndScheduledExpeditionDateBefore");
        List<Object> methodArgs = new ArrayList<Object>();
        methodArgs.add(status);
        methodArgs.add(LocalDateTime.now());
        repositoryItemReader.setArguments(methodArgs);
		final HashMap<String, Sort.Direction> sorts = new HashMap<>();
		sorts.put("scheduledExpeditionDate", Sort.Direction.ASC);
		repositoryItemReader.setSort(sorts);
		repositoryItemReader.setPageSize(1000);
		repositoryItemReader.setMaxItemCount(1000);
        return repositoryItemReader;
    }
    
    public ItemReader<GovioMessageEntity> expiredScheduledDateMessageCursor(Status status) {
        JpaCursorItemReader<GovioMessageEntity> itemReader = new JpaCursorItemReader<>();
        itemReader.setQueryString("SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE msg.status = :status AND msg.scheduledExpeditionDate < :now");
        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        itemReader.setSaveState(true);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("status", status);
        parameters.put("now", LocalDateTime.now());
        itemReader.setParameterValues(parameters);
        return itemReader;
    }

	@Bean
	public Job acquisizioneGdCJob(){
		return jobs.get("riconciliatoreRiversamentiPagoPaJob")
				.incrementer(new RunIdIncrementer())
				.start(getProfileStep())
				.next(newMessageStep())
				.build();
	}
}
