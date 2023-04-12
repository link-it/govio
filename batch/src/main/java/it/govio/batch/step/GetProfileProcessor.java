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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;

@Component
public class GetProfileProcessor extends GovioMessageAbstractProcessor {

	private Logger logger = LoggerFactory.getLogger(GetProfileProcessor.class);

	@Autowired
	private DefaultApi backendIOClient;

	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {

		logger.info("Verifica profile per il messaggio {}", item.getId());

		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode(item.getTaxcode());
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);

		try {
			LimitedProfile profileByPOST = backendIOClient.getProfileByPOST(fiscalCodePayload);
			if(profileByPOST.isSenderAllowed()) {
				logger.info("Verifica completata: spedizione consentita");
				item.setStatus(Status.RECIPIENT_ALLOWED);
			} else {
				logger.info("Verifica completata: spedizione non consentita");
				item.setStatus(Status.SENDER_NOT_ALLOWED);
			}
		} catch (HttpClientErrorException e) {
			item.setStatus(handleRestClientException(e));
		} catch (HttpServerErrorException e) {
			handleRestClientException(e);
		} catch (Exception e) {
			handleRestClientException(e);
		}
		item.setLastUpdateStatus(LocalDateTime.now());
		return item;
	}

}
