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

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
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
import org.springframework.dao.DataIntegrityViolationException;
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

	public static final String FILEPROCESSING_JOB = "FileProcessingJob";
	
	public static final String GOVIO_FILE_ITEM_WRITER = "govioFileItemWriter";

	public  static final String GOVIO_FILE_ITEM_PROCESSOR = "govioFileItemProcessor";

	public static final String GOVIO_FILE_ITEM_READER = "govioFileItemReader";

	public static final String FINALIZE_PROCESSING_FILE_TASKLET = "finalizeProcessingFileTasklet";
	
	public static final String PROMOTE_PROCESSING_FILE_STEP = "promoteProcessingFileTasklet";

	public static final String GOVIOFILE_READER_MASTER_STEP = "govioFileReaderMasterStep";
	
	public static final String LOAD_CSV_FILE_TO_DB_STEP = "loadCsvFileToDbStep";

	@Autowired
	StepBuilderFactory steps;
	
	@Value("${jobs.FileProcessingJob.steps.loadCsvFileToDbStep.executor.max-pool-size:10}")
	Integer maxPoolSize;
	
	@Value("${jobs.FileProcessingJob.steps.loadCsvFileToDbStep.executor.core-pool-size:3}")
	Integer corePoolSize;
	
	@Value("${jobs.FileProcessingJob.steps.loadCsvFileToDbStep.executor.queue-capacity:20}")
	Integer queueCapacity;
	
	@Value("${jobs.FileProcessingJob.steps.loadCsvFileToDbStep.executor.chunk-size:10}")
	Integer chunkSize;
	
	@Value("${jobs.FileProcessingJob.steps.govioFileReaderMasterStep.partitioner.grid-size:10}")
	Integer gridSize;
	
	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(maxPoolSize);
		taskExecutor.setCorePoolSize(corePoolSize);
		taskExecutor.setQueueCapacity(queueCapacity);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Bean(name = FILEPROCESSING_JOB)
	public Job fileProcessingJob(
			JobBuilderFactory jobs,
			@Qualifier(PROMOTE_PROCESSING_FILE_STEP) Step promoteProcessingFileTasklet,
			@Qualifier(GOVIOFILE_READER_MASTER_STEP) Step govioFileReaderMasterStep,
			@Qualifier(FINALIZE_PROCESSING_FILE_TASKLET) Step finalizeProcessingFileTasklet
			){
		return jobs.get(FILEPROCESSING_JOB)
				.start(promoteProcessingFileTasklet)
				.next(govioFileReaderMasterStep)
				.next(finalizeProcessingFileTasklet)
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
	@Qualifier(FINALIZE_PROCESSING_FILE_TASKLET)
	public Step finalizeProcessingFileTasklet(FinalizeFileProcessingTasklet finalizeProcessingFileTasklet) {
		return steps.get(FINALIZE_PROCESSING_FILE_TASKLET)
				.tasklet(finalizeProcessingFileTasklet)
				.build();
	}

	@Bean
	@Qualifier(GOVIOFILE_READER_MASTER_STEP)
	public Step govioFileReaderMasterStep(
			Partitioner govioFilePartitioner,
			@Qualifier(LOAD_CSV_FILE_TO_DB_STEP) Step loadCsvFileToDbStep
			) {
		return steps.get(GOVIOFILE_READER_MASTER_STEP)
				.partitioner(LOAD_CSV_FILE_TO_DB_STEP, govioFilePartitioner)
				.gridSize(gridSize)
				.step(loadCsvFileToDbStep)
				.taskExecutor(taskExecutor())
				.build();
	}

	/**
	 * Step che legge un CSV e ne memorizza il contenuto in GovioMessageFile
	 * @return
	 */
	@Bean
	@Qualifier(LOAD_CSV_FILE_TO_DB_STEP)
	public Step loadCsvFileToDbStep(
			FlatFileItemReader<GovioFileMessageEntity> govioFileItemReader,
			ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> govioFileItemProcessor,
			ItemWriter<GovioFileMessageEntity> govioFileItemWriter){
		
		return steps.get(LOAD_CSV_FILE_TO_DB_STEP)
				.<GovioFileMessageEntity, GovioFileMessageEntity>chunk(chunkSize)
				.reader(govioFileItemReader)
				.processor(govioFileItemProcessor)
				.writer(govioFileItemWriter)
				.faultTolerant()							// Skippo le lineee che sollevano il DataIntegrityViolationException
				.skip(DataIntegrityViolationException.class)
				.build();
	}

	/**
	 * Mappa le righe di un file in uno stream di GovioFileMessageEntity
	 *  
	 */
	@Bean
	@StepScope
	@Qualifier(GOVIO_FILE_ITEM_READER)
	public FlatFileItemReader<GovioFileMessageEntity> govioFileItemReader(@Value("#{stepExecutionContext[location]}") String filename) {
		FlatFileItemReader<GovioFileMessageEntity> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(new GovioFileMessageLineMapper());
		flatFileItemReader.setResource(new FileSystemResource(filename));
		return flatFileItemReader;
	}

	@Bean
	@StepScope
	@Qualifier(GOVIO_FILE_ITEM_PROCESSOR)
	public ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> govioFileItemProcessor(
			@Value("#{stepExecutionContext[template]}") Template template) {
		GovioFileItemProcessor processor = new GovioFileItemProcessor();
		processor.setGovioTemplate(template);
		return processor;
	}
	
	@Bean
	@StepScope
	@Qualifier(GOVIO_FILE_ITEM_WRITER)
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
		partitioner.setGovioFileEntities(
				govioFilesRepository.findByStatus(
						Status.PROCESSING, null
						)
			);
		
		return partitioner;
	}

}
