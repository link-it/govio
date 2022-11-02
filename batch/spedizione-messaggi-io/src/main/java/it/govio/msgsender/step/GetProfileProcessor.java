package it.govio.msgsender.step;

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
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;

@Component
public class GetProfileProcessor implements ItemProcessor<GovioMessageEntity, GovioMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GetProfileProcessor.class);

	@Value( "${rest.debugging}" )
	private boolean debugging;
	
	@Autowired
	private DefaultApi backendIOClient;
	
	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {
		
		logger.info("Verifica profile per il messaggio " + item.getId());
		
		// TODO Aggiungere il codice fiscale al messaggio
		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode("XXXXXX00A00A000A");
		
		try {
			backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
			backendIOClient.getApiClient().setDebugging(true);
			
			LimitedProfile profileByPOST = backendIOClient.getProfileByPOST(fiscalCodePayload);
			
			if(profileByPOST.isSenderAllowed()) {
				logger.info("Verifica completata: spedizione consentita");
				// Setta lo stato a "RECIPIENT_ALLOWED"
			} else {
				logger.info("Verifica completata: spedizione non consentita");
				// Setta lo stato a "SENDER_NOT_ALLOWED"
			}
		} catch (HttpClientErrorException e) {
			// Se 400 -> BAD_REQUEST
			// Se 401 -> DENIED
			// Se 403 -> FORBIDDEN
			// Se 404 -> PROFILE_NOT_EXISTS
			
			switch (e.getRawStatusCode()) {

			case 400:
				logErrorResponse(e);
				break;
			case 401:
				logErrorResponse(e);
				break;
			case 403:
				logErrorResponse(e);
				break;
			case 404:
				logger.info("Verifica completata: profilo non esistente");
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
