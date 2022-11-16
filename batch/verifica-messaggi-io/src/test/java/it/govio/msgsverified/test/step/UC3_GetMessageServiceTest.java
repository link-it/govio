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
import org.mockito.ArgumentCaptor;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
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
public class UC3_GetMessageServiceTest {
	
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
	
	final ArgumentCaptor<RequestEntity<Void>> captor = ArgumentCaptor.forClass(RequestEntity.class);

	private void setupRestTemplateMock(GovioMessageEntity message,Status status) throws Exception {
		ExternalMessageResponseWithContent response = new ExternalMessageResponseWithContent();
		response.setStatus(MessageStatusValue.fromValue(status.toString()));
		Mockito
		.when(restTemplate.exchange(captor.capture(), eq(new ParameterizedTypeReference<ExternalMessageResponseWithContent>() {})))
		.thenReturn(new ResponseEntity<ExternalMessageResponseWithContent>(response, HttpStatus.OK));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		return;
	}

	private void setupRestTemplateMockFail(GovioMessageEntity message,Status status,RestClientException exception) throws Exception {
		ExternalMessageResponseWithContent response = new ExternalMessageResponseWithContent();
		response.setStatus(MessageStatusValue.fromValue(status.toString()));
		Mockito
		.when(restTemplate.exchange(captor.capture(), eq(new ParameterizedTypeReference<ExternalMessageResponseWithContent>() {})))
		.thenThrow(exception);
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		return;
	}

	private void eseguiTest(Status status,HttpStatus error) throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(status);
		if (error != null) {
			HttpClientErrorException e = new HttpClientErrorException(error);
			setupRestTemplateMockFail(govioMessageEntity,Status.ACCEPTED,e);
		}
		else setupRestTemplateMock(govioMessageEntity,Status.ACCEPTED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);
	
		assertEquals(HttpMethod.GET, captor.getValue().getMethod());
		assertEquals(new URI("https://api.io.pagopa.it/api/v1/messages/"+govioMessageEntity.getTaxcode()+"/" + govioMessageEntity.getAppio_message_id()), captor.getValue().getUrl());
		assertEquals(govioMessageEntity.getGovioServiceInstance().getApikey(), captor.getValue().getHeaders().getFirst("Ocp-Apim-Subscription-Key"));

		assertEquals(status, processedMessage.getStatus());
	}


	@Test
	@DisplayName("UC1.1: THROTTLED")
	public void UC_3_1_THROTTLED() throws Exception {
		eseguiTest(Status.THROTTLED, null);
	}

	@Test
	@DisplayName("UC1.2: SENT")
	public void UC_1_2_SENT() throws Exception {
		eseguiTest(Status.SENT, null);
	}

	@Test
	@DisplayName("UC1.3: ACCEPTED")
	public void UC_1_3_ACCEPTED() throws Exception {
		eseguiTest(Status.ACCEPTED, null);
	}

	@Test
	@DisplayName("UC1.4: UNAUTHORIZED")
	public void UC_1_4_UNAUTHORIZED() throws Exception {
		eseguiTest(Status.ACCEPTED, HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	@DisplayName("UC1.5: FORBIDDEN")
	public void UC_1_5_FORBIDDEN() throws Exception {
		eseguiTest(Status.ACCEPTED, HttpStatus.FORBIDDEN);
	}

	@Test
	@DisplayName("UC1.6: NOT FOUND")
	public void UC_1_6_NOT_FOUND() throws Exception {
		eseguiTest(Status.ACCEPTED, HttpStatus.NOT_FOUND);
	}
}

