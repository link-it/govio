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

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.UUID;

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

}
