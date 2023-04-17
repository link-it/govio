package it.govhub.govio.mockio.api.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govhub.govio.api.mockio.beans.CreatedMessage;
import it.govhub.govio.api.mockio.beans.ExternalMessageResponseWithContent;
import it.govhub.govio.api.mockio.beans.FiscalCodePayload;
import it.govhub.govio.api.mockio.beans.LimitedProfile;
import it.govhub.govio.api.mockio.beans.MessageStatusValue;
import it.govhub.govio.api.mockio.beans.NewMessage;
import it.govhub.govio.api.mockio.spec.DefaultApi;



public class IOMockController implements DefaultApi {

	@Override
	public ResponseEntity<ExternalMessageResponseWithContent> getMessage(	String fiscalCode,	String id) {
		
		ExternalMessageResponseWithContent ret  = new ExternalMessageResponseWithContent()
				.status(MessageStatusValue.ACCEPTED);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	@Override
	public ResponseEntity<LimitedProfile> getProfileByPOST(FiscalCodePayload payload) {
		
		LimitedProfile ret = new LimitedProfile()
				.senderAllowed(true);
		
		return ResponseEntity.ok(ret);
	}

	@Override
	public ResponseEntity<CreatedMessage> submitMessageforUserWithFiscalCodeInBody(NewMessage message) {
		CreatedMessage ret = new CreatedMessage()
				.id("ID-MOCK-0");
		
		return ResponseEntity.ok(ret);
	}


}
