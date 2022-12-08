package it.govio.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private Logger logger = LoggerFactory.getLogger(UpdateFileStatusTasklet.class);
	
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
		logger.info("Promoted {} files to PROCESSED status", updateAllStatus);
		return RepeatStatus.FINISHED;
	}


	
	
	
}
