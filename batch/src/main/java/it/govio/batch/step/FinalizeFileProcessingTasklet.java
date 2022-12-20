package it.govio.batch.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.repository.GovioFilesRepository;

@Component
public class FinalizeFileProcessingTasklet implements Tasklet {

	private Logger logger = LoggerFactory.getLogger(UpdateFileStatusTasklet.class);

	@Autowired
	private GovioFilesRepository repository;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		logger.info("Finalize file processing and updating counters...");
		repository.updateProcessedFiles();
		return RepeatStatus.FINISHED;
	}
}
