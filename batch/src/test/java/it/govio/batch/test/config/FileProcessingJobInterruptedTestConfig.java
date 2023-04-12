package it.govio.batch.test.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import it.govio.batch.builders.ObservableJobBuilderFactory;
import it.govio.batch.builders.ObservableStepBuilderFactory;
import it.govio.batch.test.batch.listeners.JobStopperAfterStepListener;

/**
 * In questa configurazione facciamo in modo di interrompere l'esecuzione del
 * job, dopo il primo step di lettura e aggiornamento stato dei file "
 *
 */
@TestConfiguration
public class FileProcessingJobInterruptedTestConfig {

	@Autowired
	JobRegistry jobRegistry;

	@Autowired
	JobOperator jobOperator;

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

		factory.executionListeners.put("promoteProcessingFileTasklet",
				new JobStopperAfterStepListener(this.jobOperator));
		return factory;
	}

	
	/**
	 * Definire questo bean fa si che il jobOperator venga a conoscenza delle JobInstance esistenti e possa riavviarle.
	 * 
	 */
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
		JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
		postProcessor.setJobRegistry(jobRegistry);
		return postProcessor;
	}

}
