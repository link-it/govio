package it.govio.batch.step;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.repository.GovioFilesRepository;

public class FileReadTasklet implements Tasklet, InitializingBean {

	@Autowired
	private GovioFilesRepository govioFilesRepository;
	
	private Logger logger = LoggerFactory.getLogger(FileReadTasklet.class);

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	List<GovioFileEntity> govioFileEntities = govioFilesRepository.findByStatus(Status.CREATED);
    	
    	chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("govioFileEntities", govioFileEntities);
    	
    	List<Resource> resources = new ArrayList<>();
    	for(GovioFileEntity entity : govioFileEntities) {
    		logger.debug("Accodamento del file {} locato in ()", entity.getId(), entity.getLocation());
    		resources.add(new FileSystemResource(entity.getLocation()));
    	}
    	
    	chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("resources", resources.toArray());
    	
        return RepeatStatus.FINISHED;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		// Non ci sono bean settati da verificare
	}

}
