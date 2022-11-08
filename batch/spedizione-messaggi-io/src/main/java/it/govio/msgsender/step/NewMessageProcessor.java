package it.govio.msgsender.step;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.beans.NewMessageDefaultAddresses;
import it.pagopa.io.v1.api.beans.PaymentData;
import it.pagopa.io.v1.api.beans.Payee;
import it.govio.msgsender.entity.GovioMessageEntity.Status;

@Component
public class NewMessageProcessor implements ItemProcessor<GovioMessageEntity, GovioMessageEntity> {

	private Logger logger = LoggerFactory.getLogger(NewMessageProcessor.class);

	@Value( "${rest.debugging:false}" )
	private boolean debugging;

	@Autowired
	private DefaultApi backendIOClient;

	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {

		logger.info("Spedizione messaggio {}", item.getId());

		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);
		
		NewMessage message = new NewMessage();
		MessageContent mc = new MessageContent();

		if(item.getNoticeNumber() != null) {
			logger.debug("Presente avviso di pagamento n. {}", item.getNoticeNumber());
			PaymentData pd = new PaymentData();
			pd.setNoticeNumber(item.getNoticeNumber());
			pd.setAmount(item.getAmount());
			pd.setInvalidAfterDueDate(item.getInvalidAfterDueDate());
			if (item.getPayee()!= null) {
				Payee p = new Payee();
				p.setFiscalCode(item.getPayee());
				pd.setPayee(p);
			}
			mc.setPaymentData(pd);
		}
		
		if(item.getEmail() != null) { 
			NewMessageDefaultAddresses defaultAddress = new NewMessageDefaultAddresses();
			defaultAddress.setEmail(item.getEmail());
			message.setDefaultAddresses(defaultAddress);
		}
		
		// setto i dati rimanenti del content
		if(item.getDue_date() != null)
			mc.setDueDate(new Timestamp(item.getDue_date().toEpochSecond(ZoneOffset.UTC)));
		mc.setMarkdown(item.getMarkdown());
		mc.setSubject(item.getSubject());
		message.setContent(mc);

		message.setFiscalCode(item.getTaxcode());
		// spedizione del messaggio
		try {
			CreatedMessage submitMessageforUserWithFiscalCodeInBody = backendIOClient.submitMessageforUserWithFiscalCodeInBody(message);
			item.setAppioMessageId(submitMessageforUserWithFiscalCodeInBody.getId());
			item.setStatus(Status.SENT);
			item.setExpeditionDate(LocalDateTime.now());
			item.setLastUpdateStatus(LocalDateTime.now());
			logger.info("Messaggio spedito con successo. Id: {}", item.getAppioMessageId());
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
