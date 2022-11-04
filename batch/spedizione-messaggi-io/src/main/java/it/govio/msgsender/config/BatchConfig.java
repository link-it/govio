package it.govio.msgsender.config;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
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
	
	
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor("spring_batch_msgsender");
	}
	
	public Step getProfileStep(){
		return steps.get("getProfileStep")
				.<GovioMessageEntity, GovioMessageEntity>chunk(10)
				.reader(expiredScheduledDateMessageReader(Status.SCHEDULED))
				.processor(this.getProfileProcessor)
				.writer(messageWriter())
				//.taskExecutor(taskExecutor())
				.build();
	}
	
	public Step newMessageStep(){
		return steps.get("newMessageStep")
				.<GovioMessageEntity, GovioMessageEntity>chunk(10)
				.reader(expiredScheduledDateMessageReader(Status.RECIPIENT_ALLOWED))
				.processor(this.newMessageProcessor)
				.writer(messageWriter())
				//.taskExecutor(taskExecutor())
				.build();
	}
	
    public RepositoryItemWriter<GovioMessageEntity> messageWriter() {
        final RepositoryItemWriter<GovioMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(govioMessagesRepository);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }
	
    public RepositoryItemReader<GovioMessageEntity> expiredScheduledDateMessageReader(Status status) {
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
        return repositoryItemReader;
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
