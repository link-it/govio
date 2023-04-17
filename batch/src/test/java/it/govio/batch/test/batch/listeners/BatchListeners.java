package it.govio.batch.test.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class BatchListeners {
	
	Logger log = LoggerFactory.getLogger(BatchListeners.class);
	
	@Autowired
	private JobOperator jobOperator;

	/*@Bean(name = "promoteProcessingFileListener")
	StepExecutionListener promoteProcessingFileListener() {
		return new JobStopperStepListener(this.jobOperator);
	}*/
	
	@Bean(name = "loadCsvFileToDbStepListener")
	StepExecutionListener loadCsvFileToDbStepListener() {
		return new JobStopperStepListener(this.jobOperator);
	}
	
	
	public static class JobStopperStepListener implements StepExecutionListener {
		
		JobOperator jobOperator;
		Logger log = LoggerFactory.getLogger(JobStopperStepListener.class);
		
		public JobStopperStepListener(JobOperator jobOperator) {
			this.jobOperator = jobOperator;
		}

		@Override
		public void beforeStep(StepExecution stepExecution) {
				log.info("BEFORE STEP");
		}

		@Override
		public ExitStatus afterStep(StepExecution stepExecution) {
			log.info("AFTER STEP");
			try {
				this.jobOperator.stop(stepExecution.getJobExecutionId());
			} catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//stepExecution.getJobExecution().getI
			return stepExecution.getExitStatus();
		}
		
	}
	
	
	public static class ItemReaderStopperListener   {
		
		JobOperator jobOperator;
		Logger log = LoggerFactory.getLogger(JobStopperStepListener.class);
		
		public ItemReaderStopperListener(JobOperator jobOperator) {
			this.jobOperator = jobOperator;
		}
		
		public void beforeRead() {
			System.out.println("ItemReadListener - beforeRead");
		}

	/*	@Override
		public void afterRead(GovioFileMessageEntity item) {
			System.out.println("ItemReadListener - afterRead");
			try {
				this.jobOperator.stop(stepExecution.getJobExecutionId());
			} catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		@Override
		public void onReadError(Exception ex) {
			// TODO Auto-generated method stub
			
		}*/
		
	}


}
