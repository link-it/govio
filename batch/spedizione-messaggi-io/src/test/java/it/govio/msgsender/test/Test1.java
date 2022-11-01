package it.govio.msgsender.test;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import it.govio.msgsender.Application;
import it.govio.msgsender.entity.GovioMessagesEntity;
import it.govio.msgsender.repository.GovioMessagesRepository;
import it.pagopa.io.v1.api.DefaultApi;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.LimitedProfile;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.impl.ApiClient;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.batch.test.context.SpringBatchTest;
// @RunWith(SpringRunner.class)
// @ExtendWith(MockitoExtension.class) 
// @EnableAutoConfiguration
// @ContextConfiguration(classes = { Application.class })
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
// @TestInstance(Lifecycle.PER_CLASS)
import org.springframework.beans.factory.annotation.Autowired;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = { Application.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class Test1 {
	
	@Autowired
	private GovioMessagesRepository gmr;

	String cf;
	String id;
	String apikey;
	String markdown;
	NewMessage message;
	DefaultApi da;
	ApiClient ac;
	
	Test1() {
	cf = "AAAAAA00A00A000A";
	apikey = "17886617e07d47e8b1ba314f2f1e3052";
	id = "1";
/*	markdown = "# This is a markdown header\n\nto show how easily markdown can be converted to **HTML**\n\nRemember: this has to be a long text.",
			"payment_data": {},
			"prescription_data": {},
			"legal_data": {},
			"eu_covid_cert": {},
			"third_party_data": {},
			"due_date": "2018-10-13T00:00:00.000Z"";
			*/
	markdown = "aa";
	da = new DefaultApi();
	ac = new ApiClient();
	ac.setApiKey(apikey);
	da.setApiClient(ac);
	}
	
	@Test
	void GetProfileTest() {
    LimitedProfile lp = da.getProfile(cf);
	System.out.println(lp); 
	}
	
	
	@Test
	void SendMessageTest(){
		Optional<GovioMessagesEntity> gmeOptional = gmr.findById(1L);
		GovioMessagesEntity gme = gmeOptional.get();
		message = new NewMessage();
		message.setTimeToLive(3600);
		message.setFiscalCode(cf);
		MessageContent mc = new MessageContent();
		mc.setMarkdown(gme.getMarkdown());
		mc.setSubject(gme.getSubject());
		message.setContent(mc);
		da.submitMessageforUser(cf,message);
	}

//	@Test	
	void GetMessageTest() {
		ExternalMessageResponseWithContent mes = da.getMessage(cf,id);
		System.out.println(mes); 
	}
}
