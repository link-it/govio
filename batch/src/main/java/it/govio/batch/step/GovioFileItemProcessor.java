/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govio.batch.step;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.template.CsvTemplateApplier;
import it.govio.template.Template;
import it.govio.template.TemplateApplierFactory;
import it.govio.template.exception.TemplateFreemarkerException;
import it.govio.template.exception.TemplateValidationException;
import it.govio.template.Message;

/**
 * Processa ciascun GovioFileMessageEntity e la riga relativa del csv, applicando il template.  
 *
 */
public class GovioFileItemProcessor implements ItemProcessor<GovioFileMessageEntity,GovioFileMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GovioFileItemProcessor.class);
	private Template template;
	@Override
	public GovioFileMessageEntity process(GovioFileMessageEntity item) throws Exception {
		logger.debug("Processing line {} : [{}]", item.getLineNumber(), item.getLineRecord() );
		
		CsvTemplateApplier templateApplier = TemplateApplierFactory.buildCSVTemplateApplier(template);
		try {
			Message message = templateApplier.buildMessage(item.getLineRecord());
			GovioMessageEntity govioMessageEntity = GovioMessageEntity.builder()
					.amount(message.getAmount())
					.creationDate(LocalDateTime.now())
					.dueDate(message.getDueDate())
					.email(message.getEmail())
					.invalidAfterDueDate(message.getInvalidAfterDueDate())
					.markdown(message.getMarkdown())
					.noticeNumber(message.getNoticeNumber())
					.payee(message.getPayee())
					.scheduledExpeditionDate(message.getScheduledExpeditionDate())
					.status(Status.SCHEDULED)
					.subject(message.getSubject())
					.taxcode(message.getTaxcode())
					.build();
			
			item.setGovioMessage(govioMessageEntity);
		} catch (TemplateValidationException | TemplateFreemarkerException e) {
			item.setError(e.getMessage());
			logger.info("Errore nell'applicazione del template [numlinea: {}] [record: {}] : {}", item.getLineNumber(), item.getLineRecord(), e.getMessage());
		}
		return item;
	}
	
	public void setGovioTemplate(Template template) {
		this.template = template;
	}
}
