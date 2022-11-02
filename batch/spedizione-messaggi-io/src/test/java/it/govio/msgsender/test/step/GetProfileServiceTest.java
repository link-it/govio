package it.govio.msgsender.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
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
@DisplayName("Utility di lettura dei pagamenti")
public class GetProfileServiceTest {

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
	@DisplayName("Test verifica profilo autorizzato")
	public void getProfileSenderAllowed() throws Exception {

		// Creo su DB il messaggio da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = GovioMessageEntity.builder()
				.govioServiceInstance(serviceInstanceEntity.get())
				.markdown("Lorem Ipsum")
				.subject("Subject")
				.build();
		govioMessagesRepository.save(message);

		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode("XXXXXX00A00A000A");
		
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
		assertEquals("RECIPIENT_ALLOWED", processedMessage);

	}
}
