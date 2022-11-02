package it.govio.msgsender.test;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import it.govio.msgsender.Application;
import it.govio.msgsender.entity.GovioMessagesEntity;
import it.govio.msgsender.repository.GovioMessagesRepository;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = { Application.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class Test1 {
	
	@Autowired
	private GovioMessagesRepository gmr;

	@Autowired
	private DefaultApi backendIOClient;
	
	@Test
	void SendMessageTest(){
		
		backendIOClient.getApiClient().setApiKey("17886617e07d47e8b1ba314f2f1e3052");
		backendIOClient.getApiClient().setDebugging(true);
		
		Optional<GovioMessagesEntity> gmeOptional = gmr.findById(1L);
		GovioMessagesEntity gme = gmeOptional.get();
		NewMessage message = new NewMessage();
		//message.setTimeToLive(3600);
		message.setFiscalCode("AAAAAA00A00A000A");
		MessageContent mc = new MessageContent();
		mc.setMarkdown(gme.getMarkdown());
		mc.setSubject(gme.getSubject());
		message.setContent(mc);
		backendIOClient.submitMessageforUserWithFiscalCodeInBody(message);
	}

}
