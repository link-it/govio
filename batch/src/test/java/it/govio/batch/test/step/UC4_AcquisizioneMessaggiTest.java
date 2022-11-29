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
//import it.govio.batch.step.CsvItemProcessor;
//import it.govio.batch.step.beans.CsvItem;
//import it.govio.batch.step.beans.GovioMessage;
//import it.govio.batch.step.beans.CsvPaymentItem;

@SpringBootTest(classes = Application.class)
class UC4_AcquisizioneMessaggiTest {
//	@Autowired
//	CsvItemProcessor processProcessor;
//	@Autowired
//	private GovioServiceInstancesRepository govioServiceInstancesRepository;
//
//	@Test
//	@DisplayName("UC4.1: Acquisizione Messaggi")
//	void test() throws Exception {
//		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
//		CsvItem message = new CsvItem().builder()
//				.scheduledExpeditionDate(LocalDateTime.of(2022, 11, 22, 19, 4))
//				.taxcode("RSSMRO00A00A000A")
//				.serviceInstance(serviceInstanceEntity.get())
//				.build();
//
//		GovioMessage risultato = processProcessor.process(message);
//		checkCsvItem(risultato,serviceInstanceEntity.get());
//	}
//	
//	@Test
//	@DisplayName("UC4.2: Acquisizione Messaggi con CsvItemPayment")
//	void test_itemPayment() throws Exception {
//		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
//		CsvPaymentItem message = new CsvPaymentItem();
//		message.setScheduledExpeditionDate(LocalDateTime.of(2022, 11, 22, 19, 4));
//		message.setTaxcode("RSSMRO00A00A000A");
//		message.setServiceInstance(serviceInstanceEntity.get());
//		message.setAmount(100);
//		message.setNoticeNumber("200000000000000000");
//		message.setPayeeTaxcode("12345678901");
//		message.setInvalidAfterDueDate(false);
//		
//		GovioMessage risultato = processProcessor.process(message);
//		checkCsvPaymentItem(risultato, serviceInstanceEntity.get());
//		}
//
//	GovioMessage checkCsvItem(GovioMessage risultato,GovioServiceInstanceEntity serviceInstanceEntity) {
//		GovioMessageEntity govioMessageEntity = new GovioMessageEntity().builder()
//				.govioServiceInstance(serviceInstanceEntity)
//				.markdown("Il Comune di Empoli la informa che il 2022-11-22 alle ore 19:04 scadrà la sua Carta di Identità con numero RSSMRO00A00A000A.")
//				.subject("Il Comune di Empoli la informa che il 2022-11-22 alle ore 19:04 scadrà la sua Carta di Identità con numero RSSMRO00A00A000A.")
//				.scheduledExpeditionDate(LocalDateTime.of(2022, 11, 22, 19, 4))
//				.taxcode("RSSMRO00A00A000A")
//				.status(Status.SCHEDULED)
//				.creationDate(null)
//				.build();
//		
//		GovioMessage successo = new GovioMessage();
//		successo.setGovioFileMessageEntity(new GovioFileMessageEntity());		
//		successo.setGovioMessageEntity(govioMessageEntity);
//		
//		assertEquals(risultato.getGovioMessageEntity().getGovioServiceInstance(),successo.getGovioMessageEntity().getGovioServiceInstance());
//		assertEquals(risultato.getGovioMessageEntity().getMarkdown(),successo.getGovioMessageEntity().getMarkdown());
//		assertEquals(risultato.getGovioMessageEntity().getSubject(),successo.getGovioMessageEntity().getSubject());
//		assertEquals(risultato.getGovioMessageEntity().getScheduledExpeditionDate(),successo.getGovioMessageEntity().getScheduledExpeditionDate());
//		assertEquals(risultato.getGovioMessageEntity().getTaxcode(),successo.getGovioMessageEntity().getTaxcode());
//		assertEquals(risultato.getGovioMessageEntity().getStatus(),successo.getGovioMessageEntity().getStatus());
//		return successo;
//	}
//	
//	void checkCsvPaymentItem(GovioMessage risultato,GovioServiceInstanceEntity serviceInstanceEntity) {
//		GovioMessage successo = checkCsvItem(risultato,serviceInstanceEntity);
//		GovioMessageEntity messaggio = successo.getGovioMessageEntity();
//		messaggio.setAmount(100L);
//		messaggio.setNoticeNumber("200000000000000000");
//		messaggio.setPayee("12345678901");
//		messaggio.setInvalidAfterDueDate(false);
//		
//		assertEquals(risultato.getGovioMessageEntity().getAmount(),successo.getGovioMessageEntity().getAmount());
//		assertEquals(risultato.getGovioMessageEntity().getNoticeNumber(),successo.getGovioMessageEntity().getNoticeNumber());
//		assertEquals(risultato.getGovioMessageEntity().getPayee(),successo.getGovioMessageEntity().getPayee());
//		assertEquals(risultato.getGovioMessageEntity().getInvalidAfterDueDate(),successo.getGovioMessageEntity().getInvalidAfterDueDate());
//
//	}
}
