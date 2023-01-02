package it.govio.batch.step;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.beans.NewMessageDefaultAddresses;
import it.pagopa.io.v1.api.beans.Payee;
import it.pagopa.io.v1.api.beans.PaymentData;

@Component
public class NewMessageProcessor extends GovioMessageAbstractProcessor {

	private Logger logger = LoggerFactory.getLogger(NewMessageProcessor.class);

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
		if(item.getDueDate() != null)
			mc.setDueDate(new Timestamp(item.getDueDate().toEpochSecond(ZoneOffset.UTC)));
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
			logger.info("Messaggio spedito con successo. Id: {}", item.getAppioMessageId());
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
