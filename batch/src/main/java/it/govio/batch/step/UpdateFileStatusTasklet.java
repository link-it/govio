package it.govio.batch.step;

import org.jboss.logging.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.repository.GovioFilesRepository;

@Component
public class UpdateFileStatusTasklet implements Tasklet {

	private Logger logger = Logger.getLogger(this.getClass());
	
	@Autowired
	private GovioFilesRepository repository;
	
	private Status previousStatus;
	private Status afterStatus;
	
	public void setPreviousStatus(Status previousStatus) {
		this.previousStatus = previousStatus;
	}

	public void setAfterStatus(Status afterStatus) {
		this.afterStatus = afterStatus;
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		int updateAllStatus = repository.updateAllStatus(previousStatus, afterStatus);
		logger.info(String.format("Promoted {} files to PROCESSED status", updateAllStatus));
		return RepeatStatus.FINISHED;
	}


	
	
	
}
