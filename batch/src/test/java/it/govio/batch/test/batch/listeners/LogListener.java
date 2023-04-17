package it.govio.batch.test.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class LogListener implements StepExecutionListener {
	
	Logger log = LoggerFactory.getLogger(LogListener.class);

	@Override
	public void beforeStep(StepExecution stepExecution) {
			log.info("BEFORE STEP");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("AFTER STEP");
		
		//stepExecution.getJobExecution().getI
		return stepExecution.getExitStatus();
	}
	
}
