package it.govio.batch.test.listeners;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class StepDescriptorListener implements StepExecutionListener {
	
	private boolean stepStarted = false;
	
	public boolean isStepStarted() {
		return stepStarted;
	}

	public boolean isStepEnded() {
		return stepEnded;
	}

	private boolean stepEnded = false;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		stepStarted = true;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
			stepEnded = true;
		}
		return stepExecution.getExitStatus();
	}

}
