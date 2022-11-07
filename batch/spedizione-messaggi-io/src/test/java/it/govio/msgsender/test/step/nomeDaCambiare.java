package it.govio.msgsender.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.msgsender.Application;
import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.govio.msgsender.entity.GovioServiceInstanceEntity;
import it.govio.msgsender.repository.GovioMessagesRepository;
import it.govio.msgsender.repository.GovioServiceInstancesRepository;
import it.govio.msgsender.step.GetProfileProcessor;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;
import it.pagopa.io.v1.api.impl.ApiClient;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class nomeDaCambiare {

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

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("UC1.1: Bad request")
	public void UC_1_1_BadRequest() throws Exception {

		// Creo su DB il messaggio da inviare
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

		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.BAD_REQUEST, processedMessage.getStatus());
	}

	
	@Test
	@DisplayName("UC1.2: Profile not exists")
	public void UC_1_2_ProfileNotExists() throws Exception {

		// Creo su DB il messaggio da inviare
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

		LimitedProfile profile = null;
		
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.NOT_FOUND));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.PROFILE_NOT_EXISTS, processedMessage.getStatus());
	}

	
	
	
	@Test
	@DisplayName("UC1.3: Sender not allowed")
	public void UC_1_3_SenderNotAllowed() throws Exception {

		// Creo su DB il messaggio da inviare
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

		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(false);
		
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.SENDER_NOT_ALLOWED, processedMessage.getStatus());
	}


	@Test
	@DisplayName("UC1.4: Denied")
	public void UC_1_4_Denied() throws Exception {

		// Creo su DB il messaggio da inviare
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

		LimitedProfile profile = null;
		
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.UNAUTHORIZED));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.DENIED, processedMessage.getStatus());
	}
	
	@Test
	@DisplayName("UC1.5: Forbidden")
	public void UC_1_5_Forbidden() throws Exception {

		// Creo su DB il messaggio da inviare
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

		LimitedProfile profile = null;
		
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.FORBIDDEN));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.FORBIDDEN, processedMessage.getStatus());
	}

	
	
	
	@Test
	@DisplayName("UC1.6: Recipient allowed")
	public void UC_1_6_RecipientAllowed() throws Exception {

		// Creo su DB il messaggio da inviare
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

		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(true);
		
		ParameterizedTypeReference.forType(LimitedProfile.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		
		GovioMessageEntity processedMessage = getProfileProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.RECIPIENT_ALLOWED, processedMessage.getStatus());

	}
}
