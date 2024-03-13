/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
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

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.govio.batch.entity.GovioMessageEntity;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.MessageStatusValue;

@Component
public class GetMessageProcessor extends GovioMessageAbstractProcessor { 

	private Logger logger = LoggerFactory.getLogger(GetMessageProcessor.class);

	@Autowired
	private DefaultApi backendIOClient;

	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {
		
		logger.info("Verifica stato per il messaggio {}", item.getId());
		
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);
		try {
			FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
			fiscalCodePayload.setFiscalCode(item.getTaxcode());
			ExternalMessageResponseWithContent message = backendIOClient.getMessage(fiscalCodePayload.getFiscalCode(), item.getAppioMessageId());
			logger.info("Verifica completata: messaggio in stato {}", message.getStatus());
			MessageStatusValue status = message.getStatus();
			if(status != null)
				item.setStatus(GovioMessageEntity.Status.valueOf(status.toString()));
		} catch (HttpClientErrorException e) {
			// Ho avuto un errore client. Non cambio di stato, passo oltre
			handleRestClientException(e);
		} catch (HttpServerErrorException e) {
			handleRestClientException(e);
		} catch (Exception e) {
			handleRestClientException(e);
		}
		item.setLastUpdateStatus(OffsetDateTime.now());
		return item;
	}
	
}
