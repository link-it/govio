package it.govio.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import it.govio.batch.entity.GovioMessageEntity;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;

@Component
public class GetMessageProcessor extends GovioMessageAbstractProcessor { 

	private Logger logger = LoggerFactory.getLogger(GetMessageProcessor.class);

	@Autowired
	private DefaultApi backendIOClient;

	@Override
	public GovioMessageEntity process(GovioMessageEntity item) throws Exception {
		
		logger.info("Verifica stato per il messaggio {}", item.getId());
		
		backendIOClient.getApiClient().setApiKey(item.getGovioServiceInstance().getApikey());
		backendIOClient.getApiClient().setDebugging(debugging);
		try {
			FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
			fiscalCodePayload.setFiscalCode(item.getTaxcode());
			ExternalMessageResponseWithContent message = backendIOClient.getMessage(fiscalCodePayload.getFiscalCode(), item.getAppioMessageId());
			logger.info("Verifica completata: messaggio in stato {}", message.getStatus());
			item.setStatus(GovioMessageEntity.Status.valueOf(message.getStatus().toString()));
		} catch (HttpClientErrorException e) {
			// Ho avuto un errore client. Non cambio di stato, passo oltre
			handleRestClientException(e);
		} catch (HttpServerErrorException e) {
			handleRestClientException(e);
		} catch (Exception e) {
			handleRestClientException(e);
		}
		return item;
	}
	
}
