package it.govio.batch.step;

import java.time.LocalDateTime;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.step.beans.CsvItem;

@Component
public class CsvItemProcessor implements ItemProcessor<CsvItem, GovioFileMessageEntity> {
	/**
	 * Processor che prende una item del CSV e lo trasforma 
	 * in un GovioMessageEntity in base al template associato al 
	 * file
	 */
	public GovioFileMessageEntity process(CsvItem item) {
		
		// Implementazione dummy
		return GovioFileMessageEntity.builder()
				.govioMessage(null)
				.line_number(item.getRowNumber())
				.line_record(item.getRawData())
				.govioMessage(
						GovioMessageEntity.builder()
						.creationDate(LocalDateTime.now())
						.govioServiceInstance(item.getGovioServiceInstance())
						.markdown("Lorem ipsum dolor sit amet")
						.scheduledExpeditionDate(LocalDateTime.now().plusDays(1))
						.status(Status.SCHEDULED)
						.subject("Lorem Ipsum")
						.taxcode(item.getTaxcode())
						.build()
						)
				.build();

	}
}