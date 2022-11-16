package it.govio.batch.step;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import it.govio.batch.entity.GovioMessageEntity;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;

@Component
public class GetMessageProcessor implements ItemProcessor<GovioMessageEntity, GovioMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(GetMessageProcessor.class);

	@Value( "${rest.debugging:false}" )
	private boolean debugging;

	@Autowired
	private DefaultApi backendIOClient;

	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);
		try {
			FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
			fiscalCodePayload.setFiscalCode(item.getTaxcode());
			ExternalMessageResponseWithContent message = backendIOClient.getMessage(fiscalCodePayload.getFiscalCode(), item.getAppioMessageId());
			GovioMessageEntity gme = new GovioMessageEntity();
			gme.setStatus(item.getStatus());
			item.setStatus(GovioMessageEntity.Status.valueOf(message.getStatus().toString()));
			return gme;
		} catch (HttpClientErrorException e) {
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
				logErrorResponse(e);
				break;
			default:
				logErrorResponse(e);
				break;
			}
		} catch (HttpServerErrorException e) {
			logErrorResponse(e);
		} catch (Exception e) {
			logger.error("Internal server error", e);
		}
		return item;
	}
	
	private void logErrorResponse(HttpStatusCodeException e) {
		if(e instanceof HttpServerErrorException)
			logger.error("Ricevuto server error da BackendIO: {}", e.getMessage());
		else
			logger.warn("Ricevuto client error da BackendIO: {}", e.getMessage());
		logger.debug("HTTP Status Code: {}", e.getRawStatusCode());
		logger.debug("Status Text: {}", e.getStatusText());
		logger.debug("HTTP Headers: {}", e.getResponseHeaders());
		logger.debug("Response Body: {}", e.getResponseBodyAsString());	
	}
}
