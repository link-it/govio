package it.govio.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioTemplateEntity;
import it.govio.batch.exception.TemplateValidationException;
import it.govio.batch.step.template.GovioTemplateApplierFactory;
import it.govio.batch.step.template.TemplateApplier;

public class GovioFileItemProcessor implements ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GovioFileItemProcessor.class);
	private GovioTemplateEntity govioTemplate;
	@Override
	public GovioFileMessageEntity process(GovioFileMessageEntity item) throws Exception {
		logger.debug("Processing line {} : [{}]", item.getLineNumber(), item.getLineRecord() );
		TemplateApplier templateApplier = GovioTemplateApplierFactory.buildTemplateApplier(govioTemplate);
		
		try {
			GovioMessageEntity govioMessageEntity = templateApplier.buildGovioMessageEntity(item.getLineRecord());
			item.setGovioMessage(govioMessageEntity);
		} catch (TemplateValidationException e) {
			item.setError(e.getMessage());
			logger.info("Errore nell'applicazione del template [numlinea: {}] [record: {}] : {}", item.getLineNumber(), item.getLineRecord(), e.getMessage());
		}
		return item;
	}
	
	public void setGovioTemplate(GovioTemplateEntity govioTemplate) {
		this.govioTemplate = govioTemplate;
	}
}
