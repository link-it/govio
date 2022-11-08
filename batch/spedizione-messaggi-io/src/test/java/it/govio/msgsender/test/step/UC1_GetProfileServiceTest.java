package it.govio.msgsender.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.msgsender.Application;
import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioServiceInstanceEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.govio.msgsender.repository.GovioMessagesRepository;
import it.govio.msgsender.repository.GovioServiceInstancesRepository;
import it.govio.msgsender.step.GetProfileProcessor;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;
import it.pagopa.io.v1.api.impl.ApiClient;

@RunWith(SpringRunner.class)
@SpringBatchTest
@EnableAutoConfiguration
@ContextConfiguration(classes = { Application.class })
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class UC1_GetProfileServiceTest {

	@Mock
	private RestTemplate restTemplate;
	
	@Autowired
	private GetProfileProcessor getProfileProcessor; 
	
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
		GovioMessageEntity message = new GovioMessageBuilder()
				.buildGovioMessageEntity(serviceInstanceEntity.get(), Status.SCHEDULED, false, null, null, false, null, null);
		govioMessagesRepository.save(message);
		return message;
	}

	/**
	 * Predispone il mock del servizio IO in caso di spedizione con successo 
	 * @param govioMessageEntity 
	 * @param exception
	 * @throws Exception
	 */
	private void setupRestTemplateMock(GovioMessageEntity message, LimitedProfile profile) throws Exception {
		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode(message.getTaxcode());
		
		RequestEntity<FiscalCodePayload> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/profiles"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(fiscalCodePayload, FiscalCodePayload.class);
		// preparazione mockito
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		return;
	}
	
	
	/**
	 * Predispone il mock del servizio IO in caso di spedizione con errore 
	 * @param govioMessageEntity 
	 * @param exception
	 * @throws Exception
	 */
	private void setupRestTemplateMock(GovioMessageEntity message, RestClientException exception) throws Exception {
		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode(message.getTaxcode());
		
		RequestEntity<FiscalCodePayload> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/profiles"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(fiscalCodePayload, FiscalCodePayload.class);
		
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenThrow(exception);
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		return;
	}
	
	@Test
	@DisplayName("UC1.1: Bad request")
	public void UC_1_1_BadRequest() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new HttpClientErrorException(HttpStatus.BAD_REQUEST));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.BAD_REQUEST, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.2: Profile not exists")
	public void UC_1_2_ProfileNotExists() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new HttpClientErrorException(HttpStatus.NOT_FOUND));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.PROFILE_NOT_EXISTS, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.3: Sender not allowed")
	public void UC_1_3_SenderNotAllowed() throws Exception {
		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(false);
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, profile);
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.SENDER_NOT_ALLOWED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.4: Denied")
	public void UC_1_4_Denied() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.DENIED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.5: Forbidden")
	public void UC_1_5_Forbidden() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new HttpClientErrorException(HttpStatus.FORBIDDEN));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.FORBIDDEN, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.6: Recipient allowed")
	public void UC_1_6_RecipientAllowed() throws Exception {
		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(true);
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, profile);
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.RECIPIENT_ALLOWED, processedMessage.getStatus());
	}
	
	@Test
	@DisplayName("UC1.7: Errore 4xx non previsto")
	public void UC_1_7_Errore4xxNonPrevisto() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.SCHEDULED, processedMessage.getStatus());
	}
	
	@Test
	@DisplayName("UC1.8: Errore 5xx")
	public void UC_1_7_Errore5xx() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.SCHEDULED, processedMessage.getStatus());
	}
	
	@Test
	@DisplayName("UC1.8: Errore interno")
	public void UC_1_8_ErroreInterno() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity();
		setupRestTemplateMock(govioMessageEntity, new RestClientException("Exception"));
		GovioMessageEntity processedMessage = getProfileProcessor.process(govioMessageEntity);
		assertEquals(Status.SCHEDULED, processedMessage.getStatus());
	}
}