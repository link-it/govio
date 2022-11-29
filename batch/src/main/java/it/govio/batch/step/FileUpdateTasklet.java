package it.govio.batch.step;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import it.govio.batch.entity.GovioFileEntity;

public class FileUpdateTasklet implements Tasklet, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(FileUpdateTasklet.class);
	private List<GovioFileEntity> govioFileEntities;

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

    	for(GovioFileEntity f: govioFileEntities) {
    		// Qua si devono aggiornare i totali con le count su BD
    		// e lo stato di elaborazione.
    		logger.info("Aggiornamento del file {}", f.getId());
    	}
        return RepeatStatus.FINISHED;
    }

    public void setGovioFiles(List<GovioFileEntity> govioFileEntities) {
        this.govioFileEntities = govioFileEntities;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(govioFileEntities, "directory must be set");
    }
}
