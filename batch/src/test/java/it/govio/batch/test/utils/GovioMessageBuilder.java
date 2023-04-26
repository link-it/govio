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
package it.govio.batch.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Assert;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.GovioMessageEntityBuilder;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.entity.GovioServiceInstanceEntity;
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
	
	private static Logger log = LoggerFactory.getLogger(GovioMessageBuilder.class);
	
	public static GovioFileEntity buildFile(TemporaryFolder t, GovioServiceInstanceEntity instanceService, String i) throws IOException {
		File file = t.newFile(i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<100;x++) {
			String cf = "XXXXXX"+i+"A00Y"+String.format("%03d", x)+"Z";
			
			file1writer.write(cf + ",2022-12-31T12:00:00,2022-12-31T12:00:00,2022-12-31,Ufficio1\n");
		}
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
	
	
	/**
	 * Trasforma un numero in un codice fiscale, in modo tale da generare messaggi univoci e poterli
	 * distinguere nei mock.
	 * 
	 * Max 100 file.
	 */
	public static String indexToFiscalCode(int idx) {
		String prefix = decimalToAlphabetical(idx);
		String suffix = "00A00Y000Z";
		
		return prefix  + suffix;
	}

	/**
	 * Trasforma un numero in una stringa alfabetica di 6 caratteri.
	 * A = 0
	 * X = 24
	 * Z = Padding
	 * Contiamo in tetravigesimale
	 */
	private static String decimalToAlphabetical(int i) {
		final int start = 65;		// Lettera A in ascii
		String ret = "A";
		while (i>0) {
			int resto = (i % 24);
			i = i / 24;
			ret = ret + (char) (resto+start);
		}
		while(ret.length() < 6) {
			ret = "Z" + ret;
		}
		return ret;
	}

	public static GovioFileEntity buildFileWithUniqueCF(TemporaryFolder t, GovioServiceInstanceEntity instanceService, int fileIndex, int nRows) throws IOException {
		String strIndex = String.format("%02d", fileIndex);
		
		File file = t.newFile(strIndex+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<nRows;x++) {
			String cf = indexToFiscalCode(nRows*fileIndex+x);
			
			// Rendo univoco l'idUfficio così da poter riconoscere i messaggi che sono stati duplicati.
			file1writer.write(cf + ",2022-12-31T12:00:00,2022-12-31T12:00:00,2022-12-31,Ufficio1\n");
		}
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
