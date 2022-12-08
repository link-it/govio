package it.govio.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.step.FinalizeFileProcessingTasklet;
import it.govio.batch.step.GovioFileItemProcessor;
import it.govio.batch.step.GovioFileItemWriter;
import it.govio.batch.step.GovioFilePartitioner;
import it.govio.batch.step.UpdateFileStatusTasklet;
import it.govio.batch.step.beans.GovioFileMessageLineMapper;

@Configuration
public class FileProcessingJobConfig {

	@Autowired
	protected JobBuilderFactory jobs;

	@Autowired
	protected StepBuilderFactory steps;

	@Autowired
	private GovioFilesRepository govioFilesRepository;

	@Autowired
	private UpdateFileStatusTasklet updateFileStatusTasklet;
	
	@Autowired
	private FinalizeFileProcessingTasklet finalizeProcessingFileTasklet;

	@Autowired
	@Qualifier("govioFileItemReader")
	private FlatFileItemReader<GovioFileMessageEntity> govioFileItemReader;

	@Autowired
	@Qualifier("govioFileItemProcessor")
	private ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> govioFileItemProcessor;

	@Autowired
	@Qualifier("govioFileItemWriter")
	private ItemWriter<GovioFileMessageEntity> govioFileItemWriter;
	
	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(3);
		taskExecutor.setCorePoolSize(3);
		taskExecutor.setQueueCapacity(3);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Bean(name = "FileProcessingJob")
	public Job fileProcessingJob(){
		return jobs.get("FileProcessingJob")
				.incrementer(new RunIdIncrementer())
				.start(promoteProcessingFileTasklet())
				.next(govioFileReaderMasterStep())
				.next(finalizeProcessingFileTasklet())
				.build();
	}

	@Bean
	public Step promoteProcessingFileTasklet() {
		updateFileStatusTasklet.setPreviousStatus(Status.CREATED);
		updateFileStatusTasklet.setAfterStatus(Status.PROCESSING);
		return steps.get("promoteProcessingFileTasklet")
				.tasklet(updateFileStatusTasklet)
				.build();
	}
	
	@Bean
	@Qualifier("finalizeProcessingFileTasklet")
	public Step finalizeProcessingFileTasklet() {
		return steps.get("finalizeProcessingFileTasklet")
				.tasklet(finalizeProcessingFileTasklet)
				.build();
	}

	@Bean
	@Qualifier("govioFileReaderMasterStep")
	public Step govioFileReaderMasterStep() {
		return steps.get("govioFileReaderMasterStep")
				.partitioner("loadCsvFileToDbStep", govioFilePartitioner())
				.step(loadCsvFileToDbStep())
				.taskExecutor(taskExecutor())
				.build();
	}

	/**
	 * Step che legge un CSV e ne memorizza il contenuto in GovioMessageFile
	 * @return
	 */
	@Bean
	public Step loadCsvFileToDbStep(){
		return steps.get("loadCsvFileToDbStep")
				.<GovioFileMessageEntity, GovioFileMessageEntity>chunk(10)
				.reader(govioFileItemReader)
				.processor(govioFileItemProcessor)
				.writer(govioFileItemWriter)
				.build();
	}

	@Bean
	@StepScope
	@Qualifier("govioFileItemReader")
	public FlatFileItemReader<GovioFileMessageEntity> govioFileItemReader(@Value("#{stepExecutionContext[location]}") String filename) {
		FlatFileItemReader<GovioFileMessageEntity> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(new GovioFileMessageLineMapper());
		flatFileItemReader.setResource(new FileSystemResource(filename));
		return flatFileItemReader;
	}

	@Bean
	@StepScope
	@Qualifier("govioFileItemProcessor")
	public ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> govioFileItemProcessor(
			@Value("#{stepExecutionContext[template]}") GovioTemplateEntity template) {
		GovioFileItemProcessor processor = new GovioFileItemProcessor();
		processor.setGovioTemplate(template);
		return processor;
	}
	
	@Bean
	@StepScope
	@Qualifier("govioFileItemWriter")
	public ItemWriter<GovioFileMessageEntity> govioFileItemWriter(
			@Value("#{stepExecutionContext[id]}") long govioFileId,
			@Value("#{stepExecutionContext[serviceInstance]}") Long serviceInstanceId){
		GovioFileItemWriter govioFileItemWriter =  new GovioFileItemWriter();
		govioFileItemWriter.setGovioFileId(govioFileId);
		govioFileItemWriter.setGovioServiceInstanceId(serviceInstanceId);
		return govioFileItemWriter;
	}

	@Bean
	@StepScope
	public Partitioner govioFilePartitioner() {
		GovioFilePartitioner partitioner = new GovioFilePartitioner();
		partitioner.setGovioFileEntities(govioFilesRepository.findByStatus(Status.PROCESSING));
		return partitioner;
	}

}
