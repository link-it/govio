package it.govio.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.repository.GovioFilesRepository;

@Component
public class PromoteToProcessingTasklet implements Tasklet {

	private Logger logger = LoggerFactory.getLogger(PromoteToProcessingTasklet.class);
	
	@Autowired
	private GovioFilesRepository repository;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		int updateAllStatus = repository.updateAllStatus(Status.CREATED, Status.PROCESSING);
		if(updateAllStatus>0) {
			logger.info("Promoted {} files to PROCESSING status", updateAllStatus);
			contribution.setExitStatus(new ExitStatus("NEW_FILES_FOUND"));  
		} else {
			contribution.setExitStatus(new ExitStatus("NEW_FILES_NOT_FOUND"));  
		}
		return RepeatStatus.FINISHED;
	}
}
