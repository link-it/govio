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
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.beans.PaymentData;
import it.pagopa.io.v1.api.beans.Payee;
import it.govio.msgsender.entity.GovioMessageEntity.Status;

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
		Integer time_to_live = 3600;
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(true);

		switch (item.getStatus()) {
		case SCHEDULED:
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

			switch (e.getRawStatusCode()) {

			case 400:
				logErrorResponse(e);
				item.setStatus(Status.BAD_REQUEST);
				break;
			case 401:
				logErrorResponse(e);
				item.setStatus(Status.DENIED);
				break;
			case 403:
				logErrorResponse(e);
				item.setStatus(Status.FORBIDDEN);
				break;
			case 404:
				logger.info("Verifica completata: profilo non esistente");
				item.setStatus(Status.PROFILE_NOT_EXISTS);
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
		
		case RECIPIENT_ALLOWED:
			NewMessage message = new NewMessage();
			MessageContent mc = new MessageContent();
			// se presente costruisco il PaymentData
			if (item.getPayee()!= null) {
				PaymentData pd = new PaymentData();
				if (item.getAmount() != null) {
					pd.setAmount(item.getAmount());
				}
				if (item.getInvalid_after_due_date() != null) {
					pd.setInvalidAfterDueDate(item.getInvalid_after_due_date());
				}
				if (item.getNotice_number() != null) {
					pd.setNoticeNumber(item.getNotice_number());
				}
				Payee p = new Payee();
				if (fiscalCodePayload.getFiscalCode() != null) {
					p.setFiscalCode(fiscalCodePayload.getFiscalCode());
				}
				pd.setPayee(p);
				mc.setPaymentData(pd);
			}
			// setto i dati rimanenti del content
			if (item.getMarkdown() != null) {
				mc.setMarkdown(item.getMarkdown());
			}
			if (item.getSubject() != null) {
				mc.setSubject(item.getSubject());
			}
			message.setContent(mc);
			// setto i dati rimanenti del messaggio
			message.setTimeToLive(time_to_live);
			message.setFiscalCode(fiscalCodePayload.getFiscalCode());
			// spedizione del messaggio
			try {
			CreatedMessage submitMessageforUserWithFiscalCodeInBody = backendIOClient.submitMessageforUserWithFiscalCodeInBody(message);
			item.setAppio_message_id(submitMessageforUserWithFiscalCodeInBody.getId());
			} catch (HttpClientErrorException e) {
				switch (e.getRawStatusCode()) {
				case 400:
					logErrorResponse(e);
					item.setStatus(Status.BAD_REQUEST);
					break;
				case 401:
					logErrorResponse(e);
					item.setStatus(Status.DENIED);
					break;
				case 403:
					logErrorResponse(e);
					item.setStatus(Status.FORBIDDEN);
					break;
				case 404:
					logger.info("Verifica completata: profilo non esistente");
					item.setStatus(Status.PROFILE_NOT_EXISTS);
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
		default:
			break;
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
