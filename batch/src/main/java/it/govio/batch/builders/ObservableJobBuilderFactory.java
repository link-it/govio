package it.govio.batch.builders;

import org.slf4j.LoggerFactory;

import java.util.HashMap;

import org.slf4j.Logger;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;

/**
 *	 Factory per JobBuilder che hooka dei listener configurati dall'applicazione, in base al nome
 *  dello step da generare.
 * 
 *  Utile in fase di test, o per il supporto a un sistema di monitoraggio e configurazione dei batch.
 *
 */
public class ObservableJobBuilderFactory extends JobBuilderFactory {
	
	public HashMap<String, JobExecutionListener> listeners = new HashMap<>();
	
	Logger log = LoggerFactory.getLogger(ObservableJobBuilderFactory.class);

	public ObservableJobBuilderFactory(JobRepository jobRepository) {
		super(jobRepository);
	}


	@Override
	public JobBuilder get(String name) {
		log.debug("Using custom ObservableJobBuilderFactory.");
		JobBuilder ret = super.get(name);
		
		JobExecutionListener l = listeners.get(name);
		if (l != null) {
			ret.listener(l);
		}
		
		return ret;
	}
	
}
