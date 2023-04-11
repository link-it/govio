package it.govio.batch.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.rules.TemporaryFolder;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioMessageEntity.GovioMessageEntityBuilder;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.pagopa.io.v1.api.beans.Payee;

public class GovioMessageBuilder {
	
	public GovioMessageEntity buildGovioMessageEntity(GovioServiceInstanceEntity serviceInstanceEntity, Status status, boolean due_date, Long amount, String noticeNumber, boolean invalidAfterDueDate, Payee payee, String email) throws URISyntaxException {
		GovioMessageEntityBuilder messageEntity = GovioMessageEntity.builder()
				.govioServiceInstance(serviceInstanceEntity)
				.govhubUserId(1l)
				.markdown("Lorem Ipsum")
				.subject("Subject")
				.taxcode("AAAAAA00A00A000A")
				.status(status)
				.creationDate(LocalDateTime.now())
				.scheduledExpeditionDate(LocalDateTime.now());
		if (due_date) messageEntity.dueDate(LocalDateTime.now().plusDays(3));
		if (amount != null && amount > 0) {
			messageEntity.amount(amount);
			messageEntity.noticeNumber(noticeNumber);
			messageEntity.invalidAfterDueDate(invalidAfterDueDate);
		}
		if (payee != null) messageEntity.payee(payee.getFiscalCode());
		if (email != null) messageEntity.email(email);
		
		switch (status) {
		case SENT:
		case THROTTLED:
		case ACCEPTED:
			messageEntity.expeditionDate(LocalDateTime.now());
			messageEntity.appioMessageId(UUID.randomUUID().toString());
			break;
		default:
			break;
		}
		
		GovioMessageEntity message = messageEntity.build();
		return message;
	}

	public static GovioFileEntity buildFile(TemporaryFolder t, GovioServiceInstanceEntity instanceService, String i) throws IOException {
		File file = t.newFile(i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<100;x++)
			file1writer.write("XXXXXX"+i+"A00Y"+String.format("%03d", x)+"Z,2022-12-31T12:00:00,2022-12-31T12:00:00,2022-12-31,Ufficio1\n");
		file1writer.close();
	
		GovioFileEntity govioFile1 = GovioFileEntity.builder()
				.creationDate(LocalDateTime.now())
				.govioServiceInstance(instanceService)
				.govhubUserId(1l)
				.location(file.toPath().toString())
				.name(file.getName())
				.status(GovioFileEntity.Status.CREATED)
				.build();
	
		return govioFile1;
	}

}
