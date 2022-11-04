package it.govio.msgsender.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
import it.govio.msgsender.step.NewMessageProcessor;
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.impl.ApiClient;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class UC2_NewMessageServiceTest {

	@Mock
	private RestTemplate restTemplate;

	@Autowired
	private NewMessageProcessor newMessageProcessor; 
	
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
	@DisplayName("UC2.6: Messaggio minimale (no avviso, no scadenza, no payee, no email)")
	public void UC2_6_MinimalMessageOk() throws Exception {

		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = GovioMessageEntity.builder()
				.govioServiceInstance(serviceInstanceEntity.get())
				.markdown("Lorem Ipsum")
				.subject("Subject")
				.taxcode("AAAAAA00A00A000A")
				.status(Status.RECIPIENT_ALLOWED)
				.creationDate(LocalDateTime.now())
				.scheduledExpeditionDate(LocalDateTime.now())
				.build();
		govioMessagesRepository.save(message);

		NewMessage newMessage = new NewMessage();
		newMessage.setFiscalCode(message.getTaxcode());
		MessageContent content = new MessageContent();
		content.setMarkdown(message.getMarkdown());
		content.setSubject(message.getSubject());
		newMessage.setContent(content);
		
		RequestEntity<NewMessage> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/messages"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(newMessage, NewMessage.class);

		CreatedMessage createdMessage = new CreatedMessage();
		createdMessage.setId(UUID.randomUUID().toString());
		
		ParameterizedTypeReference.forType(CreatedMessage.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<CreatedMessage>() {})))
		.thenReturn(new ResponseEntity<CreatedMessage>(createdMessage, HttpStatus.CREATED));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
		
		
		GovioMessageEntity processedMessage = newMessageProcessor.process(message);
		
		// TODO verificare lo stato del messaggio, quando disponibile nell'entity
		assertEquals(GovioMessageEntity.Status.SENT, processedMessage.getStatus());

	}
}
