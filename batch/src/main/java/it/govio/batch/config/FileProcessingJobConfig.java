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
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.step.FinalizeFileProcessingTasklet;
import it.govio.batch.step.GovioFileItemProcessor;
import it.govio.batch.step.GovioFileItemWriter;
import it.govio.batch.step.GovioFilePartitioner;
import it.govio.batch.step.PromoteToProcessingTasklet;
import it.govio.batch.step.beans.GovioFileMessageLineMapper;
import it.govio.template.Template;

@Configuration
public class FileProcessingJobConfig {

	@Autowired
	protected StepBuilderFactory steps;

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
	public Job fileProcessingJob(
			JobBuilderFactory jobs,
			@Qualifier("promoteProcessingFileTasklet") Step promoteProcessingFileTasklet,
			@Qualifier("govioFileReaderMasterStep") Step govioFileReaderMasterStep,
			@Qualifier("finalizeProcessingFileTasklet") Step finalizeProcessingFileTasklet
			){
		return jobs.get("FileProcessingJob")
				.incrementer(new RunIdIncrementer())
				.start(promoteProcessingFileTasklet)
				.on("NEW_FILES_NOT_FOUND")
				.end()
				.from(promoteProcessingFileTasklet)
				.on("NEW_FILES_FOUND")
				.to(govioFileReaderMasterStep)
				.next(finalizeProcessingFileTasklet)
				.end()
				.build();
	}

	@Bean
	@Qualifier("promoteProcessingFileTasklet")
	public Step promoteProcessingFileTasklet(PromoteToProcessingTasklet updateFileStatusTasklet) {
		return steps.get("promoteProcessingFileTasklet")
				.tasklet(updateFileStatusTasklet)
				.build();
	}
	
	@Bean
	@Qualifier("finalizeProcessingFileTasklet")
	public Step finalizeProcessingFileTasklet(FinalizeFileProcessingTasklet finalizeProcessingFileTasklet) {
		return steps.get("finalizeProcessingFileTasklet")
				.tasklet(finalizeProcessingFileTasklet)
				.build();
	}

	@Bean
	@Qualifier("govioFileReaderMasterStep")
	public Step govioFileReaderMasterStep(
			Partitioner govioFilePartitioner,
			@Qualifier("loadCsvFileToDbStep") Step loadCsvFileToDbStep
			) {
		return steps.get("govioFileReaderMasterStep")
				.partitioner("loadCsvFileToDbStep", govioFilePartitioner)
				.step(loadCsvFileToDbStep)
				.taskExecutor(taskExecutor())
				.build();
	}

	/**
	 * Step che legge un CSV e ne memorizza il contenuto in GovioMessageFile
	 * @return
	 */
	@Bean
	@Qualifier("loadCsvFileToDbStep")
	public Step loadCsvFileToDbStep(
			FlatFileItemReader<GovioFileMessageEntity> govioFileItemReader,
			ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> govioFileItemProcessor,
			ItemWriter<GovioFileMessageEntity> govioFileItemWriter){
		return steps.get("loadCsvFileToDbStep")
				.<GovioFileMessageEntity, GovioFileMessageEntity>chunk(10)
				.reader(govioFileItemReader)
				.processor(govioFileItemProcessor)
				.writer(govioFileItemWriter)
				.build();
	}

	/**
	 * Mappa le righe di un file in uno stream di GovioFileMessageEntity
	 *  
	 */
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
			@Value("#{stepExecutionContext[template]}") Template template) {
		GovioFileItemProcessor processor = new GovioFileItemProcessor();
		processor.setGovioTemplate(template);
		return processor;
	}
	
	@Bean
	@StepScope
	@Qualifier("govioFileItemWriter")
	public ItemWriter<GovioFileMessageEntity> govioFileItemWriter(
			@Value("#{stepExecutionContext[id]}") long govioFileId,
			@Value("#{stepExecutionContext[serviceInstance]}") Long serviceInstanceId,
			@Value("#{stepExecutionContext[govhubUserId]}") Long govhubUserId){
		GovioFileItemWriter govioFileItemWriter =  new GovioFileItemWriter();
		govioFileItemWriter.setGovioFileId(govioFileId);
		govioFileItemWriter.setGovioServiceInstanceId(serviceInstanceId);
		govioFileItemWriter.setGovhubUserId(govhubUserId);
		return govioFileItemWriter;
	}

	@Bean
	@StepScope
	public Partitioner govioFilePartitioner(GovioFilesRepository govioFilesRepository) {
		GovioFilePartitioner partitioner = new GovioFilePartitioner();
		partitioner.setGovioFileEntities(govioFilesRepository.findByStatus(Status.PROCESSING));
		return partitioner;
	}

}
