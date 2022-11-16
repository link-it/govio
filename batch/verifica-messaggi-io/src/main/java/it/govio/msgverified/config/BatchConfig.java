package it.govio.msgverified.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
import it.govio.msgverified.entity.GovioMessageEntity;
import it.govio.msgverified.entity.GovioMessageEntity.Status;
import it.govio.msgverified.repository.GovioMessagesRepository;
import it.govio.msgverified.step.GetMessageProcessor;

import org.springframework.data.domain.Sort;

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
		    return new SimpleAsyncTaskExecutor("spring_batch_msgverified");
		}
		
		public Step getMessageStep(){
			return steps.get("verificaMessaggiGovIOJob")
					.<GovioMessageEntity, GovioMessageEntity>chunk(10)
					.reader(messageReader())
					.processor(this.getMessageProcessor)
					.writer(messageWriter())
					.build();
		}

		private RepositoryItemReader<GovioMessageEntity> messageReader() {
	    	final RepositoryItemReader<GovioMessageEntity> repositoryItemReader = new RepositoryItemReader<>();
	    	repositoryItemReader.setRepository(govioMessagesRepository);
	    	repositoryItemReader.setMethodName("findByStatus");
	    	List<Object> methodArgs = new ArrayList<Object>();
	    	methodArgs.add("SENT");
	    	methodArgs.add("ACCEPTED");
	    	methodArgs.add("THROTTLED");
	    	methodArgs.add(LocalDateTime.now());
	    	repositoryItemReader.setArguments(methodArgs);
	    	repositoryItemReader.setPageSize(1000);
	    	repositoryItemReader.setMaxItemCount(1000);
	    	return repositoryItemReader;
	    }
		
	    private RepositoryItemWriter<GovioMessageEntity> messageWriter() {
	        final RepositoryItemWriter<GovioMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
	        repositoryItemWriter.setRepository(govioMessagesRepository);
	        repositoryItemWriter.setMethodName("save");
	        return repositoryItemWriter;
	    }

		@Bean
		public Job acquisizioneGdCJob(){
			return jobs.get("verificaMessaggiGovIOJob")
					.incrementer(new RunIdIncrementer())
					.start(getMessageStep())
					.build();
		}

	}

		
