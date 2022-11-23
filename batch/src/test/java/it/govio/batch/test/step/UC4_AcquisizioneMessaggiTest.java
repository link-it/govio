package it.govio.batch.test.step;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.step.CsvItemProcessor;
import it.govio.batch.test.utils.GovioMessageBuilder;



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
		GovioMessageEntity message = new GovioMessageBuilder().buildGovioMessageEntity(serviceInstanceEntity.get(), Status.CREATED, false, null, null, false, null, null);
		message.setExpeditionDate(LocalDateTime.of(2022, 11, 22, 19, 4));
		message.setTaxcode("RSSMRO00A00A000A");
		String risultato = processProcessor.process(message);
		String successo = ("Il Comune di Empoli la informa che il 2022-11-22 alle ore 19:04 scadrà la sua Carta di Identità con numero RSSMRO00A00A000A.");
		assertEquals(risultato,successo);

	}
}
