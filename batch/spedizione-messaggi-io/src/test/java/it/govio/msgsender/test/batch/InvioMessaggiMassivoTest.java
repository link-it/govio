package it.govio.msgsender.test.batch;

import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.msgsender.Application;
import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioServiceInstanceEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.govio.msgsender.repository.GovioMessagesRepository;
import it.govio.msgsender.repository.GovioServiceInstancesRepository;
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;
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
public class InvioMessaggiMassivoTest {
	
	@Mock
	private RestTemplate restTemplate;

	@Autowired
	@InjectMocks
	private ApiClient apiClient;

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

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
	
	@Test
	public void spedizioneMessaggiOK() throws Exception {
	    
		// Caricamento messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);

		govioMessagesRepository.deleteAll();
		
		for(int i=0; i<100; i++) {
			GovioMessageEntity message = GovioMessageEntity.builder()
					.govioServiceInstance(serviceInstanceEntity.get())
					.markdown("Lorem Ipsum")
					.subject("Subject")
					.taxcode("XXXAAA00A00A000A")
					.scheduledExpeditionDate(LocalDateTime.now().minusDays(1))
					.creationDate(LocalDateTime.now().minusDays(2))
					.status(Status.SCHEDULED)
					.build();
			govioMessagesRepository.save(message);
		}
		
		// Mock delle chiamate ai servizi IO
		FiscalCodePayload fiscalCodePayload = new FiscalCodePayload();
		fiscalCodePayload.setFiscalCode("XXXAAA00A00A000A");
		RequestEntity<FiscalCodePayload> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/profiles"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", serviceInstanceEntity.get().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(fiscalCodePayload, FiscalCodePayload.class);

		LimitedProfile profile = new LimitedProfile();
		profile.setSenderAllowed(true);
		
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenReturn(new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK));
		//.thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));
		
		NewMessage newMessage = new NewMessage();
		newMessage.setFiscalCode("XXXAAA00A00A000A");
		MessageContent content = new MessageContent();
		content.setMarkdown("Lorem Ipsum");
		content.setSubject("Subject");
		newMessage.setContent(content);
		
		RequestEntity<NewMessage> newMessageRequest = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/messages"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", serviceInstanceEntity.get().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(newMessage, NewMessage.class);

		CreatedMessage createdMessage = new CreatedMessage();
		createdMessage.setId(UUID.randomUUID().toString());
		
		ParameterizedTypeReference.forType(CreatedMessage.class);
		Mockito
		.when(restTemplate.exchange(eq(newMessageRequest), eq(new ParameterizedTypeReference<CreatedMessage>() {})))
		.thenReturn(new ResponseEntity<CreatedMessage>(createdMessage, HttpStatus.CREATED));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		
        //Controllo che tutti i messaggi siano spediti con successo e aggiornati conseguentemente.
        
        List<GovioMessageEntity> findAll = govioMessagesRepository.findAll();
        for(GovioMessageEntity entity : findAll) {
        	Assert.assertNotNull(entity.getExpeditionDate());
        	Assert.assertNotNull(entity.getLastUpdateStatus());
        	Assert.assertEquals(Status.SENT, entity.getStatus());
        	Assert.assertNotNull(entity.getAppioMessageId());
        }
	}
	
	
}
