package it.govio.batch.test.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

public class DatabaseStopperAfterStepListener implements StepExecutionListener {
	
		JobOperator jobOperator;
		
		Logger log = LoggerFactory.getLogger(DatabaseStopperAfterStepListener.class);
		
		boolean fired = false;
		
		public DatabaseStopperAfterStepListener(JobOperator jobOperator) {
			this.jobOperator = jobOperator;
		}

		
		@Override
		public ExitStatus afterStep(StepExecution stepExecution) {
			log.info("Running StepListener for Job [{}] after step [{}]", stepExecution.getJobExecution().getJobInstance().getJobName(), stepExecution.getStepName());
			
			if ( ! fired ) {
				log.info("Stopping H2 DATABASE");
				try {
					this.jobOperator.stop(stepExecution.getJobExecutionId());
					fired = true;
				} catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				log.info("H2 DATABASE Already stopped");
			}
			
			return stepExecution.getExitStatus();
		}
		
		@Override
		public void beforeStep(StepExecution stepExecution) {	}
}
