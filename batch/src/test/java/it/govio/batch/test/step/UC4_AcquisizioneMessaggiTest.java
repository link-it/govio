package it.govio.batch.test.step;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioFileMessageEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.step.CsvItemProcessor;
import it.govio.batch.step.beans.CsvItem;
import it.govio.batch.step.beans.GovioMessage;



@SpringBootTest(classes = Application.class)
public class UC4_AcquisizioneMessaggiTest {
	@Autowired
	CsvItemProcessor processProcessor;
	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Test
	@DisplayName("UC4.1: Acquisizione Messaggi")
	void test() throws Exception {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		CsvItem message = new CsvItem();
		message.setScheduledExpeditionDate(LocalDateTime.of(2022, 11, 22, 19, 4));
		message.setTaxcode("RSSMRO00A00A000A");
		message.setServiceInstance(serviceInstanceEntity.get());
		GovioMessage risultato = processProcessor.process(message);
		
		GovioMessage successo = new GovioMessage();
		
		successo.setGovioFileMessageEntity(new GovioFileMessageEntity());
		
		GovioMessageEntity govioMessageEntity = new GovioMessageEntity().builder()
				.govioServiceInstance(serviceInstanceEntity.get())
				.markdown("Il Comune di Empoli la informa che il 2022-11-22 alle ore 19:04 scadrà la sua Carta di Identità con numero RSSMRO00A00A000A.")
				.subject("Il Comune di Empoli la informa che il 2022-11-22 alle ore 19:04 scadrà la sua Carta di Identità con numero RSSMRO00A00A000A.")
				.scheduledExpeditionDate(LocalDateTime.of(2022, 11, 22, 19, 4))
				.taxcode("RSSMRO00A00A000A")
				.status(Status.SCHEDULED)
				.creationDate(null)
				.build();
		
		successo.setGovioMessageEntity(govioMessageEntity);
		assertEquals(risultato.getGovioMessageEntity().getGovioServiceInstance(),successo.getGovioMessageEntity().getGovioServiceInstance());
		assertEquals(risultato.getGovioMessageEntity().getMarkdown(),successo.getGovioMessageEntity().getMarkdown());
		assertEquals(risultato.getGovioMessageEntity().getSubject(),successo.getGovioMessageEntity().getSubject());
		assertEquals(risultato.getGovioMessageEntity().getScheduledExpeditionDate(),successo.getGovioMessageEntity().getScheduledExpeditionDate());
		assertEquals(risultato.getGovioMessageEntity().getTaxcode(),successo.getGovioMessageEntity().getTaxcode());
		assertEquals(risultato.getGovioMessageEntity().getStatus(),successo.getGovioMessageEntity().getStatus());
	}
}
