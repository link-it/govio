package it.govio.msgverified.config;

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

import it.govio.msgverified.entity.GovioMessageEntity;
import it.govio.msgverified.entity.GovioMessageEntity.Status;
import it.govio.msgverified.repository.GovioMessagesRepository;
import it.govio.msgverified.step.GetMessageProcessor;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;

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
		private GetMessageProcessor getMessageProcessor;
		
		@Autowired
		private GovioMessagesRepository govioMessagesRepository;
		
		@Autowired
		private EntityManager entityManager;
		
		
		public TaskExecutor taskExecutor() {
		    return new SimpleAsyncTaskExecutor("spring_batch_msgsender");
		}
		
		public Step getMessageStep(){
			return steps.get("getMessageStep")
					.<GovioMessageEntity, GovioMessageEntity>chunk(10)
					.reader(expiredScheduledDateMessageCursor(Status.THROTTLED,Status.SENT,Status.ACCEPTED))
					.processor(this.getMessageProcessor)
					.writer(messageWriter())
					.build();
		}
		
		/*
	    private RepositoryItemReader<GovioMessageEntity> messageReader(Status sent,Status accepted, Status throttled) {
	        final RepositoryItemReader<GovioMessageEntity> repositoryItemReader = new RepositoryItemReader<>();
	        repositoryItemReader.setRepository(govioMessagesRepository);
	        repositoryItemReader.setMethodName("findAllByStatusAndExpeditionDateAndLastUpdateStatusBefore");
	        List<Object> methodArgs = new ArrayList<Object>();
	        methodArgs.add(accepted);
	        methodArgs.add(sent);
	        methodArgs.add(throttled);
	        methodArgs.add(LocalDateTime.now());
	        repositoryItemReader.setArguments(methodArgs);
			final HashMap<String, Sort.Direction> sorts = new HashMap<>();
			sorts.put("expeditionDate", Sort.Direction.ASC);
			sorts.put("lastUpdateStatus", Sort.Direction.ASC);
			repositoryItemReader.setSort(sorts);
			repositoryItemReader.setPageSize(1000);
			repositoryItemReader.setMaxItemCount(1000);
	        return repositoryItemReader;
	    }
	    */

	    private RepositoryItemWriter<GovioMessageEntity> messageWriter() {
	        final RepositoryItemWriter<GovioMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
	        repositoryItemWriter.setRepository(govioMessagesRepository);
	        repositoryItemWriter.setMethodName("save");
	        return repositoryItemWriter;
	    }
	    
	    
	    public ItemReader<GovioMessageEntity> expiredScheduledDateMessageCursor(Status sent,Status accepted, Status throttled) {
	        JpaCursorItemReader<GovioMessageEntity> itemReader = new JpaCursorItemReader<>();
	        itemReader.setQueryString("SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE msg.status = :sent AND msg.status = :accepted AND msg.status = :throttled AND (msg.expeditionDate < :now OR msg.lastUpdateStatus < :now)");
	        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
	        itemReader.setSaveState(true);
	        Map<String, Object> parameters = new HashMap<String, Object>();
	        parameters.put("sent", sent);
	        parameters.put("accepted", accepted);
	        parameters.put("throttled", throttled);
	        parameters.put("now", LocalDateTime.now());
	        itemReader.setParameterValues(parameters);
	        return itemReader;
	    }
	    
	    @Bean
		public Job acquisizioneGdCJob(){
			return jobs.get("verificaMessaggiGovIOJob")
					.incrementer(new RunIdIncrementer())
					.start(getMessageStep())
					.build();
		}

	}

		
