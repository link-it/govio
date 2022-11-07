package it.govio.msgsender.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import java.net.URI;
import java.time.LocalDateTime;
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
import org.springframework.batch.test.JobLauncherTestUtils;
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
	
	public <T> void util( ResponseEntity<LimitedProfile> re,GovioMessageEntity.Status check) throws Exception{
		// creazione del messaggio
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = GovioMessageEntity.builder()
				.govioServiceInstance(serviceInstanceEntity.get())
				.markdown("Lorem Ipsum")
				.subject("Subject")
				.taxcode("AAAAAA00A00A000A")
				.status(Status.SCHEDULED)
				.creationDate(LocalDateTime.now())
				.scheduledExpeditionDate(LocalDateTime.now())
				.build();
		govioMessagesRepository.save(message);

		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode("AAAAAA00A00A000A");
		
		RequestEntity<FiscalCodePayload> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/profiles"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(fiscalCodePayload, FiscalCodePayload.class);
		// preparazione mockito
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(re);
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		// test
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// check
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(check, processedMessage.getStatus());
		return;
	}
	
	
	public void utilFail( HttpClientErrorException e,GovioMessageEntity.Status check) throws Exception{
		// creazione del messaggio
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = GovioMessageEntity.builder()
				.govioServiceInstance(serviceInstanceEntity.get())
				.markdown("Lorem Ipsum")
				.subject("Subject")
				.taxcode("AAAAAA00A00A000A")
				.status(Status.SCHEDULED)
				.creationDate(LocalDateTime.now())
				.scheduledExpeditionDate(LocalDateTime.now())
				.build();
		govioMessagesRepository.save(message);

		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode("AAAAAA00A00A000A");
		
		RequestEntity<FiscalCodePayload> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/profiles"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(fiscalCodePayload, FiscalCodePayload.class);
		// preparazione mockito
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenThrow(e);
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		// test
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// check
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(check, processedMessage.getStatus());
		return;
	}
	
	
	

	@Test
	@DisplayName("UC1.1: Bad request")
	public void UC_1_1_BadRequest() throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
		utilFail(e, GovioMessageEntity.Status.BAD_REQUEST);
	}

	@Test
	@DisplayName("UC1.2: Profile not exists")
	public void UC_1_2_ProfileNotExists() throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.NOT_FOUND);
		utilFail(e, GovioMessageEntity.Status.PROFILE_NOT_EXISTS);
	}

	@Test
	@DisplayName("UC1.3: Sender not allowed")
	public void UC_1_3_SenderNotAllowed() throws Exception {
		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(false);
		util(new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK), GovioMessageEntity.Status.SENDER_NOT_ALLOWED);
	}

	@Test
	@DisplayName("UC1.4: Denied")
	public void UC_1_4_Denied() throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
		utilFail(e, GovioMessageEntity.Status.DENIED);
	}

	@Test
	@DisplayName("UC1.5: Forbidden")
	public void UC_1_5_Forbidden() throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.FORBIDDEN);
		utilFail(e, GovioMessageEntity.Status.FORBIDDEN);
	}

	@Test
	@DisplayName("UC1.6: Recipient allowed")
	public void UC_1_6_RecipientAllowed() throws Exception {
		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(true);
		util(new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK), GovioMessageEntity.Status.RECIPIENT_ALLOWED);

	}
}