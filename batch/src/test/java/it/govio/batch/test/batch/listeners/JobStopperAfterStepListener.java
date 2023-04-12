package it.govio.batch.test.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

public class JobStopperAfterStepListener implements StepExecutionListener {
	
		JobOperator jobOperator;
		
		Logger log = LoggerFactory.getLogger(JobStopperAfterStepListener.class);
		
		boolean fired = false;
		
		public JobStopperAfterStepListener(JobOperator jobOperator) {
			this.jobOperator = jobOperator;
		}

		
		@Override
		public void beforeStep(StepExecution stepExecution) {	}

		
		@Override
		public ExitStatus afterStep(StepExecution stepExecution) {
			log.info("JobStopperAfterStepListener::afterStep");
			if ( ! fired ) {
				log.info("Stopping job [{}] after step [{}]", stepExecution.getJobExecution().getJobInstance().getJobName(), stepExecution.getStepName());
				try {
					this.jobOperator.stop(stepExecution.getJobExecutionId());
					fired = true;
				} catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				log.info("Job [{}] already stopped one time after step [{}]", stepExecution.getJobExecution().getJobInstance().getJobName(), stepExecution.getStepName());
			}
			
			
			return stepExecution.getExitStatus();
		}
}
