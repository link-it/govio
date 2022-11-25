package it.govio.batch.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.exception.BackendioRuntimeException;
import it.govio.batch.repository.GovioFileMessagesRepository;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.step.CsvItemProcessor;
import it.govio.batch.step.FileUpdateTasklet;
import it.govio.batch.step.beans.CsvItem;

@Configuration
public class FileProcessingJobConfig {

	@Autowired
	protected JobBuilderFactory jobs;

	@Autowired
	protected StepBuilderFactory steps;

	@Autowired
	private CsvItemProcessor csvItemProcessor;

	@Autowired
	private GovioFileMessagesRepository govioFileMessagesRepository;
	
	private List<GovioFileEntity> govioFileEntities;
	
	private Resource[] resources;

	private GovioFilesRepository govioFilesRepository;
	
	protected TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor("spring_batch_fileprocessor");
	}

	@Bean(name = "FileProcessingJob")
	public Job verificaMessaggiIO(){
		
    	govioFileEntities = govioFilesRepository.findByStatus(Status.CREATED);
    	List<Resource> resourceList = new ArrayList<>();
    	for(GovioFileEntity entity : govioFileEntities) {
    		resourceList.add(new FileSystemResource(entity.getLocation()));
    	}
		this.resources = (Resource[]) resourceList.toArray();
		
		return jobs.get("FileProcessingJob")
				.incrementer(new RunIdIncrementer())
				.start(processCsvStep())
				.next(processCsvStep())
				.next(updateFileStep())
				.build();
	}

	@Bean
	public Step processCsvStep(){
		return steps.get("processCsvStep")
				.<CsvItem, GovioFileMessageEntity>chunk(10)
				.reader(multiResourceItemReader())
				.processor(this.csvItemProcessor)
				.writer(messageWriter())
				.faultTolerant()
				.skip(BackendioRuntimeException.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}

	@Bean
	public Step updateFileStep() {
		FileUpdateTasklet task = new FileUpdateTasklet();
		task.setGovioFiles(govioFileEntities);
		return steps.get("updateFileStep")
				.tasklet(task)
				.build();
	}

	@Bean
	public MultiResourceItemReader<CsvItem> multiResourceItemReader() 
	{
		MultiResourceItemReader<CsvItem> resourceItemReader = new MultiResourceItemReader<CsvItem>();
		resourceItemReader.setResources(resources);
		resourceItemReader.setDelegate(reader());
		return resourceItemReader;
	}


	@Bean
	public FlatFileItemReader<CsvItem> reader() 
	{
		//Create reader instance
		FlatFileItemReader<CsvItem> reader = new FlatFileItemReader<CsvItem>();

		//Set number of lines to skips. Use it if file has header rows.
		reader.setLinesToSkip(1);   

		//Configure how each line will be parsed and mapped to different values
		reader.setLineMapper(new DefaultLineMapper() {
			{
				//3 columns in each row
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "id", "firstName", "lastName" });
					}
				});
				//Set values in Employee class
				setFieldSetMapper(new BeanWrapperFieldSetMapper<CsvItem>() {
					{
						setTargetType(CsvItem.class);
					}
				});
			}
		});
		return reader;
	}

	protected AsyncItemProcessor<CsvItem, GovioFileMessageEntity> asyncProcessor(CsvItemProcessor itemProcessor) {
		AsyncItemProcessor<CsvItem, GovioFileMessageEntity> asyncItemProcessor = new AsyncItemProcessor<>();
		asyncItemProcessor.setTaskExecutor(taskExecutor());
		asyncItemProcessor.setDelegate(itemProcessor);
		return asyncItemProcessor;
	}

	protected RepositoryItemWriter<GovioFileMessageEntity> messageWriter() {
		final RepositoryItemWriter<GovioFileMessageEntity> repositoryItemWriter = new RepositoryItemWriter<>();
		repositoryItemWriter.setRepository(govioFileMessagesRepository);
		repositoryItemWriter.setMethodName("save");
		return repositoryItemWriter;
	}
}
