package it.govio.msgsender.test;

//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;

//import java.io.IOException;
//
//import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.web.client.HttpClientErrorException;

//import com.fasterxml.jackson.core.exc.StreamReadException;
//import com.fasterxml.jackson.databind.DatabindException;
//import com.fasterxml.jackson.databind.ObjectMapper;

import it.govio.msgsender.Application;
//import it.pagopa.io.v1.api.DefaultApi;
//import it.pagopa.io.v1.api.beans.CreatedMessage;
//import it.pagopa.io.v1.api.beans.LimitedProfile;
//import it.pagopa.io.v1.api.beans.MessageContent;
//import it.pagopa.io.v1.api.beans.NewMessage;
//import it.pagopa.io.v1.api.beans.ProblemJson;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = { Application.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public class Test1 {
	
//	@Autowired
//	private DefaultApi backendIOClient;
//	
//	@Test
//	void GetProfileTest() {
//		backendIOClient.getApiClient().setApiKey("17886617e07d47e8b1ba314f2f1e3052");
//		backendIOClient.getApiClient().setDebugging(true);
//	    LimitedProfile lp = backendIOClient.getProfile("AAAAAA00A00A000A");
//	    assertTrue(lp.isSenderAllowed());
//	}
//	
//	@Test
//	void SendMessageOkTest(){
//		backendIOClient.getApiClient().setApiKey("17886617e07d47e8b1ba314f2f1e3052");
//		backendIOClient.getApiClient().setDebugging(true);
//		
//		NewMessage message = new NewMessage();
//		message.setFiscalCode("AAAAAA00A00A000A");
//		MessageContent mc = new MessageContent();
//		mc.setMarkdown("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer mauris lorem, euismod quis sapien ac, pretium ornare augue. Nullam pulvinar ultricies nisi suscipit pulvinar. Maecenas dictum tortor vehicula ante accumsan, sed tempus quam mollis. Morbi dictum purus risus, vel aliquet mauris pretium at. Mauris purus odio, iaculis non metus eu, iaculis interdum dui. Phasellus et hendrerit magna. Ut vehicula turpis in elit dapibus euismod. Cras imperdiet pulvinar diam. Fusce semper massa diam, eu sagittis magna tincidunt quis. Mauris sit amet dui suscipit, volutpat urna a, molestie elit. Mauris in erat a justo rhoncus scelerisque.");
//		mc.setSubject("Lorem ipsum dolor sit amet");
//		message.setContent(mc);
//		CreatedMessage submitMessageforUserWithFiscalCodeInBody = backendIOClient.submitMessageforUserWithFiscalCodeInBody(message);
//		assertNotNull(submitMessageforUserWithFiscalCodeInBody.getId());
//	}
//	
//	@Test
//	void CSendMessagefNotAuthorizedTest() throws StreamReadException, DatabindException, IOException{
//		backendIOClient.getApiClient().setApiKey("17886617e07d47e8b1ba314f2f1e3052");
//		backendIOClient.getApiClient().setDebugging(true);
//		
//		NewMessage message = new NewMessage();
//		message.setFiscalCode("BBBBBB00A00A000A");
//		MessageContent mc = new MessageContent();
//		mc.setMarkdown("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer mauris lorem, euismod quis sapien ac, pretium ornare augue. Nullam pulvinar ultricies nisi suscipit pulvinar. Maecenas dictum tortor vehicula ante accumsan, sed tempus quam mollis. Morbi dictum purus risus, vel aliquet mauris pretium at. Mauris purus odio, iaculis non metus eu, iaculis interdum dui. Phasellus et hendrerit magna. Ut vehicula turpis in elit dapibus euismod. Cras imperdiet pulvinar diam. Fusce semper massa diam, eu sagittis magna tincidunt quis. Mauris sit amet dui suscipit, volutpat urna a, molestie elit. Mauris in erat a justo rhoncus scelerisque.");
//		mc.setSubject("Lorem ipsum dolor sit amet");
//		message.setContent(mc);
//		
//		HttpClientErrorException error = assertThrows(
//				HttpClientErrorException.class,
//		           () ->  backendIOClient.submitMessageforUserWithFiscalCodeInBody(message),
//		           "Atteso 403 NotAuthorized"
//		    );
//		
//		assertEquals(error.getRawStatusCode(), 403);
//		
//		ObjectMapper om = new ObjectMapper();
//		ProblemJson readValue = om.readValue(error.getResponseBodyAsByteArray(), ProblemJson.class);
//		assertEquals("You are not allowed to issue requests for the recipient.", readValue.getDetail());
//	}
	
}
