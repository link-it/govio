package it.govio.batch.step;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govio.batch.repository.GovioFilesRepository;

@Component
public class FinalizeFileProcessingTasklet implements Tasklet {

	@Autowired
	private GovioFilesRepository repository;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		repository.updateProcessedFiles();
		return RepeatStatus.FINISHED;
	}
}
