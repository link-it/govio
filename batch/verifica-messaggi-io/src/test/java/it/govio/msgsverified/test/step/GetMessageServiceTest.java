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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
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
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
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
	
	
	private GovioMessageEntity buildGovioMessageEntity() throws URISyntaxException {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = buildGovioMessageEntity(serviceInstanceEntity.get(), Status.THROTTLED);
		govioMessagesRepository.save(message);
		return message;
	}

	public GovioMessageEntity buildGovioMessageEntity(GovioServiceInstanceEntity serviceInstanceEntity, Status status) throws URISyntaxException {
		GovioMessageEntityBuilder messageEntity = GovioMessageEntity.builder()
				.govioServiceInstance(serviceInstanceEntity)
				.markdown("markdown")
				.subject("subject")
				.taxcode("AAAAAA00A00A000A")
				.status(status);
		GovioMessageEntity message = messageEntity.build();
		return message;
	}

	private void setupRestTemplateMock(GovioMessageEntity message,Status status) throws Exception {
		// newMessage.setFeatureLevelType("ADVANCED");

		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode("AAAAAA00A00A000A");
		
		RequestEntity<Void> request = RequestEntity
				.get(new URI("https://api.io.pagopa.it/api/v1/messages/"))
				.accept(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.build();
		GovioMessageEntity response = new GovioMessageEntity();
		response.setId(1L);
		response.setTaxcode("AAAAAA00A00A000A");
		response.setStatus(status);
		response.setSubject(message.getSubject());
		response.setMarkdown(message.getMarkdown());
		
		ParameterizedTypeReference.forType(GovioMessageEntity.class);

		// preparazione mockito
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<GovioMessageEntity>() {})))
		.thenReturn(new ResponseEntity<GovioMessageEntity>(message, HttpStatus.OK));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		return;
	}
	
	@Test
	@DisplayName("UC1.1: good THROTTLED")
	public void UC_1_1_GoodRequest() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity,Status.THROTTLED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.THROTTLED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.2: good PROCESSED")
	public void UC_1_2_GoodProcessed() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity,Status.PROCESSED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.PROCESSED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.3: good PROCESSED")
	public void UC_1_3_GoodRejected() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity,Status.REJECTED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.REJECTED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.3: good FAILED")
	public void UC_1_3_GoodRFAILED() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity,Status.FAILED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
		assertEquals(Status.FAILED, processedMessage.getStatus());
	}
}
