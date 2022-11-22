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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.step.GetProfileProcessor;
import it.govio.batch.step.GovioMessageAbstractProcessor;
import it.govio.batch.step.NewMessageProcessor;
import it.govio.batch.step.ProcessProcessor;


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
	
	//@Autowired
	//private ProcessProcessor processProcessor;
	
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
	
	/*
	@Bean(name = "JobAcquisizioneMessaggiIO")
	public Job acquisizioneMessaggiIO(){
		return jobs.get("JobAcquisizioneMessaggiIO")
				.incrementer(new RunIdIncrementer())
				.start(acquireMessagesStep())
				.build();
	}

	public Step acquireMessagesStep() {
    return steps.get("step1").<GovioCSVEntity, Future<GovioFileEntity>>chunk(5)
            .reader(reader())
            .processor(this.processProcessor)
            .writer(writer())
            .build();
      }
*/
/*
	  private ItemWriter<? super Future<GovioFileEntity>> writer() {
				AsyncItemWriter<GovioFileEntity> asyncItemWriter = new AsyncItemWriter<>();
		//	    asyncItemWriter.messageWriter2();
			    return asyncItemWriter;
			}
	    private RepositoryItemWriter<GovioFileEntity> messageWriter2() {
	        final RepositoryItemWriter<GovioFileEntity> repositoryItemWriter = new RepositoryItemWriter<>();
	        repositoryItemWriter.setRepository(govioFilesRepository);
	        repositoryItemWriter.setMethodName("save");
	        return repositoryItemWriter;
	    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FlatFileItemReader<GovioCSVEntity> reader()
	  {
	    //Create reader instance
	    FlatFileItemReader<GovioCSVEntity> reader = new FlatFileItemReader<GovioCSVEntity>();
	     
	    //Set input file location
	    reader.setResource(new FileSystemResource("src/test/resources/csv-test"));
	     
	    //Set number of lines to skips. Use it if file has header rows.
	    reader.setLinesToSkip(1);   
	     
	    //Configure how each line will be parsed and mapped to different values
	    reader.setLineMapper(new DefaultLineMapper() {
	      {
	        setLineTokenizer(new DelimitedLineTokenizer() {
	          {
	            setNames(new String[] { "taxcode", "expedition_date"});
	          }
	        });
	        //Set values in GovioFileEntity class
	        setFieldSetMapper(new BeanWrapperFieldSetMapper<GovioFileEntity>() {
	          {
	            setTargetType(GovioFileEntity.class);
	          }
	        });
	      }
	    });
	    return reader;
	  }
*/


	
	
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
