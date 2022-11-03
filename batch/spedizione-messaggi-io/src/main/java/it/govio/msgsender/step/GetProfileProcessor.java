package it.govio.msgsender.step;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;

@Component
public class GetProfileProcessor implements ItemProcessor<GovioMessageEntity, GovioMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GetProfileProcessor.class);

	@Value( "${rest.debugging:false}" )
	private boolean debugging;

	@Autowired
	private DefaultApi backendIOClient;

	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {

		// TODO Aggiungere stampe di debugging
		// TODO settare la data e l'ora dopo il cambiamento di stato

		logger.info("Verifica profile per il messaggio " + item.getId());

		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode(item.getTaxcode());
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);

		try {
			LimitedProfile profileByPOST = backendIOClient.getProfileByPOST(fiscalCodePayload);
			if(profileByPOST.isSenderAllowed()) {
				logger.info("Verifica completata: spedizione consentita");
				item.setStatus(Status.RECIPIENT_ALLOWED);
				item.setLastUpdateStatus(LocalDateTime.now());
			} else {
				logger.info("Verifica completata: spedizione non consentita");
				item.setStatus(Status.SENDER_NOT_ALLOWED);
				item.setLastUpdateStatus(LocalDateTime.now());
			}
		} catch (HttpClientErrorException e) {

			switch (e.getRawStatusCode()) {

			case 400:
				logErrorResponse(e);
				item.setStatus(Status.BAD_REQUEST);
				item.setLastUpdateStatus(LocalDateTime.now());
				break;
			case 401:
				logErrorResponse(e);
				item.setStatus(Status.DENIED);
				item.setLastUpdateStatus(LocalDateTime.now());
				break;
			case 403:
				logErrorResponse(e);
				item.setStatus(Status.FORBIDDEN);
				item.setLastUpdateStatus(LocalDateTime.now());
				break;
			case 404:
				logger.info("Verifica completata: profilo non esistente");
				item.setStatus(Status.PROFILE_NOT_EXISTS);
				item.setLastUpdateStatus(LocalDateTime.now());
				break;				
			default:
				logErrorResponse(e);
				break;
			}
		} catch (HttpServerErrorException e) {
			logger.error("Ricevuto server error da BackendIO: " + e.getMessage());
			logger.debug("HTTP Status Code: " + e.getRawStatusCode());
			logger.debug("Status Text: " + e.getStatusText());
			logger.debug("HTTP Headers: " + e.getResponseHeaders());
			logger.debug("Response Body: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			logger.error("Internal server error: " + e.getMessage(), e);
		}

		return item;
	}


	private void logErrorResponse(HttpStatusCodeException e) {
		logger.warn("Ricevuto error da BackendIO: " + e.getMessage());
		logger.debug("HTTP Status Code: " + e.getRawStatusCode());
		logger.debug("Status Text: " + e.getStatusText());
		logger.debug("HTTP Headers: " + e.getResponseHeaders());
		logger.debug("Response Body: " + e.getResponseBodyAsString());	
	}
}
