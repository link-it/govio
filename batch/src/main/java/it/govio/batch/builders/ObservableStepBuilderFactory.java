package it.govio.batch.builders;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *	 Factory per StepBuilder che hooka dei listener configurati dall'applicazione, in base al nome
 *  dello step da generare.
 *  
 *  Configura i listener per l'intera esecuzione dello step, e i listeners per i writer e i readers.
 * 
 *  Utile in fase di test, o per il supporto a un sistema di monitoraggio e configurazione dei batch.
 *
 */
public class ObservableStepBuilderFactory extends StepBuilderFactory {
	
	public HashMap<String, StepExecutionListener> executionListeners = new HashMap<>();
	
	public HashMap<String, Set<StepListener>> stepListeners = new HashMap<>();
	
	// Le riporto qui perchè private nel padre.
	JobRepository jobRepository;

	PlatformTransactionManager transactionManager;
	
	Logger log = LoggerFactory.getLogger(ObservableJobBuilderFactory.class);

	public ObservableStepBuilderFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		super(jobRepository, transactionManager);
		
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	@Override
	public StepBuilder get(String name) {
		log.debug("Using Custom ObservableStepBuilderFactory.");
		
		ObservableStepBuilder builder = (ObservableStepBuilder) 
				new ObservableStepBuilder(name).
					repository(jobRepository).
					transactionManager(	transactionManager);
		
		StepExecutionListener l = this.executionListeners.get(name);
		if (l != null) {
			builder.listener(l);
		}
		
		Set<StepListener> sl = this.stepListeners.get(name);
		if (sl != null) {
			builder.listeners = Set.copyOf(sl);
		}
		
		return builder;
	}
}
