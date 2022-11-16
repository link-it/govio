package it.govio.msgverified.step;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import it.govio.msgverified.entity.GovioMessageEntity;
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
		// TODO Auto-generated method stub
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);
		try {
			FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
			fiscalCodePayload.setFiscalCode(item.getTaxcode());
			 ExternalMessageResponseWithContent message = backendIOClient.getMessage(fiscalCodePayload.getFiscalCode(), item.getAppio_message_id());
			 GovioMessageEntity gme = new GovioMessageEntity();
			 gme.setStatus(item.getStatus());
			 /* risposte possibili:
		      "ACCEPTED": the message has been accepted and will be processed for
		      delivery;
		        we'll try to store its content in the user's inbox and notify him on his preferred channels
		      "THROTTLED": a temporary failure caused a retry during the message
		      processing;
		        any notification associated with this message will be delayed for a maximum of 7 days
		      "FAILED": a permanent failure caused the process to exit with an error, no
		      notification will be sent for this message

		      "PROCESSED": the message was succesfully processed and is now stored in
		      the user's inbox;
		        we'll try to send a notification for each of the selected channels
		      "REJECTED": either the recipient does not exist, or the sender has been
		      blocked
*/
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
