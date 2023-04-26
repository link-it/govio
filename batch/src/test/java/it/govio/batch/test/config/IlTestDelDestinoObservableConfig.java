package it.govio.batch.test.config;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import it.govio.batch.config.SendMessagesJobConfig;
import it.govio.batch.test.batch.builders.ObservableJobBuilderFactory;
import it.govio.batch.test.batch.builders.ObservableStepBuilderFactory;
import it.govio.batch.test.listeners.StepDescriptorListener;

/**
 * In questa configurazione registriamo un solo listener che imposta lo stato di esecuzione del
 * primo step del SendMessagesJob
 * 
 */
@TestConfiguration
public class IlTestDelDestinoObservableConfig {

	public final StepDescriptorListener getProfileDescriptor  = new StepDescriptorListener();

	@Bean
	@Primary
	public JobBuilderFactory jobBuilderFactory(JobRepository repository) {
		var factory = new ObservableJobBuilderFactory(repository);
		return factory;
	}

	
	@Bean
	@Primary
	public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository,	PlatformTransactionManager transactionManager) {
		var factory = new ObservableStepBuilderFactory(jobRepository, transactionManager);
		factory.executionListeners.put(SendMessagesJobConfig.GETPROFILE_STEPNAME,	getProfileDescriptor);
		return factory;
	}
	
	
}
