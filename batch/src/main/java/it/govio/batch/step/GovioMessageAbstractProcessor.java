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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.BackendioRuntimeException;


@Component
public abstract class GovioMessageAbstractProcessor implements ItemProcessor<GovioMessageEntity, GovioMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GovioMessageAbstractProcessor.class);

    @Value( "${govio.consumer.retry-after-default:3600}" )
	protected int defaultRetryTimer;
    @Value( "${ govio.consumer.retry-after-max:10000}" )
	protected int maxRetryTimer;

	@Value( "${rest.debugging:false}" )
	protected boolean debugging;

	protected Status handleRestClientException(HttpClientErrorException e) throws HttpClientErrorException, InterruptedException {
		logErrorResponse(e);
		switch (e.getRawStatusCode()) {
		case 400:
			return Status.BAD_REQUEST;
		case 401:
			return Status.DENIED;
		case 403:
			return Status.FORBIDDEN;
		case 404:
			return Status.PROFILE_NOT_EXISTS;
		case 429:
			HttpHeaders responseHeaders = e.getResponseHeaders();
			String value = null;
			
			if(responseHeaders != null) {
				value = responseHeaders.getFirst("Retry-After");
			}

			int sleepTime;
			if (value == null) sleepTime = defaultRetryTimer;
			else {
				if (Integer.parseInt(value) > maxRetryTimer) sleepTime = maxRetryTimer;
				else sleepTime = Integer.parseInt(value);
		}
			Thread.sleep(sleepTime);
			logger.error("Ricevuta eccezione 429, aspettato {} prima di riprovare",sleepTime );
			return Status.SCHEDULED;
		default:
			logger.error("Ricevuto client error non previsto da BackendIO: {}", e.getMessage());
			throw e;
		}
	}
	
	protected void handleRestClientException(HttpStatusCodeException e) throws HttpClientErrorException {
		logErrorResponse(e);
		throw e;
	}
	
	protected void handleRestClientException(Exception e) {
		logger.error("Internal server error", e);
		throw new BackendioRuntimeException(e);
	}


	protected void logErrorResponse(HttpStatusCodeException e) {
		if(e instanceof HttpServerErrorException)
			logger.error("Ricevuto server error da BackendIO: {}", e.getMessage());
		else
			logger.debug("Ricevuto client error da BackendIO: {}", e.getMessage());
		logger.debug("HTTP Status Code: {}", e.getRawStatusCode());
		logger.debug("Status Text: {}", e.getStatusText());
		logger.debug("HTTP Headers: {}", e.getResponseHeaders());
		logger.debug("Response Body: {}", e.getResponseBodyAsString());	
	}
}
