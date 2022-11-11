package it.govio.msgsverified.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.msgverified.entity.GovioMessageEntity.GovioMessageEntityBuilder;
import it.govio.msgverified.Application;
import it.govio.msgverified.entity.GovioMessageEntity;
import it.govio.msgverified.entity.GovioServiceInstanceEntity;
import it.govio.msgverified.entity.GovioMessageEntity.Status;
import it.govio.msgverified.repository.GovioMessagesRepository;
import it.govio.msgverified.repository.GovioServiceInstancesRepository;
import it.govio.msgverified.step.GetMessageProcessor;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.MessageStatusValue;
import it.pagopa.io.v1.api.impl.ApiClient;

@RunWith(SpringRunner.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { Application.class })
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class GetMessageServiceTest {
	
	@Mock
	private RestTemplate restTemplate;
	
	@Autowired
	private GetMessageProcessor getMessageProcessor; 
	
	@Autowired
	@InjectMocks
	private ApiClient apiClient;

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioMessagesRepository govioMessagesRepository;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;
		
	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions();
	}
	
	
	private GovioMessageEntity buildGovioMessageEntity(Status status) throws URISyntaxException {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioServiceInstanceEntity service = serviceInstanceEntity.get();
		GovioMessageEntityBuilder messageEntity = GovioMessageEntity.builder()
				.govioServiceInstance(service)
				.markdown("markdown")
				.subject("subject")
				.taxcode("AAAAAA00A00A000A")
				.status(status)
				.appio_message_id("101010101");
		GovioMessageEntity message = messageEntity.build();
		govioMessagesRepository.save(message);
		return message;
	}
	

	private void setupRestTemplateMock(GovioMessageEntity message,Status status) throws Exception {
		/*
		response.setId(1L);
		response.setTaxcode("AAAAAA00A00A000A");
		response.setStatus(status);
		response.setSubject(message.getSubject());
		response.setMarkdown(message.getMarkdown());
		*/

		RequestEntity<Void> request  = RequestEntity
				.get(new URI("https://api.io.pagopa.it/api/v1/messages/AAAAAA00A00A000A/101010101"))
				.accept(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.build();

		ExternalMessageResponseWithContent response = new ExternalMessageResponseWithContent();
		response.setStatus(MessageStatusValue.fromValue(status.toString()));
		ParameterizedTypeReference.forType(ExternalMessageResponseWithContent.class);
		// preparazione mockito
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<ExternalMessageResponseWithContent>() {})))
		.thenReturn(new ResponseEntity<ExternalMessageResponseWithContent>(response, HttpStatus.OK));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		return;
	}

	@Test
	@DisplayName("UC1.1: good THROTTLED")
	public void UC_1_1_GoodRequest() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.THROTTLED);
		setupRestTemplateMock(govioMessageEntity,Status.THROTTLED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.THROTTLED, processedMessage.getStatus());
	}
/*
	@Test
	@DisplayName("UC1.2: good PROCESSED")
	public void UC_1_2_GoodProcessed() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.PROCESSED);
		setupRestTemplateMock(govioMessageEntity,Status.PROCESSED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.PROCESSED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.3: good PROCESSED")
	public void UC_1_3_GoodRejected() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.REJECTED);
		setupRestTemplateMock(govioMessageEntity,Status.REJECTED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.REJECTED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.3: good FAILED")
	public void UC_1_3_GoodRFAILED() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.FAILED);
		setupRestTemplateMock(govioMessageEntity,Status.FAILED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.FAILED, processedMessage.getStatus());
	}
	
401 - Unauthorized

403 - Forbidden.

404 - No message found for the provided ID.

429 - Too many requests

*/
}
