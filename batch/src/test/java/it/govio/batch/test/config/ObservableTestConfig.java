package it.govio.batch.test.config;

import java.util.Set;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import it.govio.batch.builders.ObservableJobBuilderFactory;
import it.govio.batch.builders.ObservableStepBuilderFactory;
import it.govio.batch.test.batch.listeners.ChunkLogListener;
import it.govio.batch.test.batch.listeners.LogListener;


@TestConfiguration
public class ObservableTestConfig {

	@Bean
	@Primary
	public JobBuilderFactory jobBuilderFactory(JobRepository repository) {
		var factory = new ObservableJobBuilderFactory(repository);
		
		return factory;
	}

	@Bean
	@Primary
	public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		
		var factory = new ObservableStepBuilderFactory(jobRepository, transactionManager);
		
		factory.executionListeners.put("promoteProcessingFileTasklet", new LogListener());
		
		factory.stepListeners.put("loadCsvFileToDbStep", Set.of( new ChunkLogListener()));
		
		return factory;
	}
	
}
