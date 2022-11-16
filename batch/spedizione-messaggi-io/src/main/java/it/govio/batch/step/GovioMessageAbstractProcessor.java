package it.govio.batch.step;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;

@Component
public abstract class GovioMessageAbstractProcessor implements ItemProcessor<GovioMessageEntity, GovioMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GovioMessageAbstractProcessor.class);

	@Value( "${rest.debugging:false}" )
	protected boolean debugging;

	protected Status handleRestClientException(HttpClientErrorException e) throws HttpClientErrorException {
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
		default:
			logger.error("Ricevuto client error non previsto da BackendIO: {}", e.getMessage());
			throw e;
		}
	}
	
	protected void handleRestClientException(HttpStatusCodeException e) throws HttpClientErrorException {
		logErrorResponse(e);
		throw e;
	}
	
	protected void handleRestClientException(Exception e) throws Exception {
		logger.error("Internal server error", e);
		throw e;
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