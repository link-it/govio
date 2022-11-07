	package it.govio.msgsender.test.step;

	import static org.junit.jupiter.api.Assertions.assertEquals;
	import static org.mockito.ArgumentMatchers.eq;

	import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
	import org.springframework.web.util.DefaultUriBuilderFactory;

	import it.govio.msgsender.Application;
	import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioMessageEntity.GovioMessageEntityBuilder;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.govio.msgsender.entity.GovioServiceInstanceEntity;
	import it.govio.msgsender.repository.GovioMessagesRepository;
	import it.govio.msgsender.repository.GovioServiceInstancesRepository;
	import it.govio.msgsender.step.NewMessageProcessor;
	import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.MessageContent;
	import it.pagopa.io.v1.api.beans.NewMessage;
	import it.pagopa.io.v1.api.impl.ApiClient;
import it.pagopa.io.v1.api.beans.Payee;

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

		
		GovioMessageEntity buildMessage(long due_date,Integer amount,String noticeNumber,boolean invalidAfterDueDate,Payee p,String email) throws URISyntaxException {
			Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
			GovioMessageEntityBuilder messageEntity = GovioMessageEntity.builder()
					.govioServiceInstance(serviceInstanceEntity.get())
					.markdown("Lorem Ipsum")
					.subject("Subject")
					.taxcode("AAAAAA00A00A000A")
					.status(Status.RECIPIENT_ALLOWED)
					.creationDate(LocalDateTime.now())
					.scheduledExpeditionDate(LocalDateTime.now());
					if (due_date > 0) messageEntity.due_date(LocalDateTime.now().plusDays(due_date));
					if (amount > 0) {
						messageEntity.amount(amount);
						messageEntity.noticeNumber(noticeNumber);
						messageEntity.invalidAfterDueDate(invalidAfterDueDate);
					}
					if (p != null) messageEntity.payee(p.getFiscalCode());
					if (email != null) messageEntity.email(email);
			GovioMessageEntity message = messageEntity.build();
			govioMessagesRepository.save(message);
			return message;
		}
				
		void util(GovioMessageEntity message, HttpStatus response,GovioMessageEntity.Status check) throws Exception {

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
			.thenReturn(new ResponseEntity<CreatedMessage>(createdMessage, response));
			Mockito
			.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
			
			GovioMessageEntity processedMessage = newMessageProcessor.process(message);
			
			assertEquals(check, processedMessage.getStatus());

		}
		
		void utilFail(GovioMessageEntity message,HttpClientErrorException e,GovioMessageEntity.Status check) throws Exception {
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
			.thenThrow(e);
			Mockito
			.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
			
			
			GovioMessageEntity processedMessage = newMessageProcessor.process(message);
			
			assertEquals(check, processedMessage.getStatus());

		}


		@Test
		@DisplayName("UC2.1: Bad request")
		public void UC2_1_BadRequest () throws Exception {
			HttpClientErrorException e = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
			utilFail(buildMessage(0, 0, null, false, null, null), e, GovioMessageEntity.Status.BAD_REQUEST);
		}

		@Test
		@DisplayName("UC2.2: Profile not exists")
		public void UC2_2_ProfileNotExists () throws Exception {
			HttpClientErrorException e = new HttpClientErrorException(HttpStatus.NOT_FOUND);
			utilFail(buildMessage(0, 0, null, false, null, null), e, GovioMessageEntity.Status.PROFILE_NOT_EXISTS);
		}
		/*
		@Test
		@DisplayName("UC2.3: Sender not allowed")
		public void UC2_3_SenderNotAllowed() throws Exception {
			util(HttpStatus.OK, GovioMessageEntity.Status.SENDER_NOT_ALLOWED);
		}
		*/
		@Test
		@DisplayName("UC2.4: Denied)")
		public void UC2_4_Denied() throws Exception {
			HttpClientErrorException e = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
			utilFail(buildMessage(0, 0, null, false, null, null), e, GovioMessageEntity.Status.DENIED);
		}

		
		@Test
		@DisplayName("UC2_5_Forbidden")
		public void UC2_5_Forbidden() throws Exception {
			HttpClientErrorException e = new HttpClientErrorException(HttpStatus.FORBIDDEN);
			utilFail(buildMessage(0, 0, null, false, null, null), e, GovioMessageEntity.Status.FORBIDDEN);
		}

		@Test
		@DisplayName("UC2.6: Messaggio minimale (no avviso, no scadenza, no payee, no email)")
		public void UC2_6_MessaggioMinimale() throws Exception {
			util(buildMessage(0, 0, null, false, null, null), HttpStatus.CREATED, GovioMessageEntity.Status.SENT);
		}

		@Test
		@DisplayName("UC2.7: Messaggio con avviso")
		public void UC2_7_MessaggioConAvviso() throws Exception {
			util(buildMessage(0, 1000, "122222222222222222", false, null, null), HttpStatus.CREATED, GovioMessageEntity.Status.SENT);
		}

		@Test
		@DisplayName("UC2.8: Messaggio con scadenza")
		public void UC2_8_MessaggioConScadenza() throws Exception {
			Payee p = new Payee().fiscalCode("12345678901");
			util(buildMessage(1L, 1, "122222222222222222", false, p, null), HttpStatus.CREATED, GovioMessageEntity.Status.SENT);
		}

		@Test
		@DisplayName("UC2.9: Messaggio con payee")
		public void UC2_9_MessaggioConpayee() throws Exception {
			Payee p = new Payee().fiscalCode("12345678901");
			util(buildMessage(0, 0, null, false, p, null), HttpStatus.CREATED, GovioMessageEntity.Status.SENT);
		}

		@Test
		@DisplayName("UC2.10: Messaggio con email")
		public void UC2_10_MessaggioConEmail() throws Exception {
			String email = new String("email@gmail.com");
			util(buildMessage(0, 0, null, false, null, email), HttpStatus.CREATED, GovioMessageEntity.Status.SENT);
		}

		@Test
		@DisplayName("UC2.11: Messaggio non consegnato (eg http 500 o errore rete o altro)")
		public void UC2_11_NonConseganto() throws Exception {
			HttpClientErrorException e = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
			utilFail(buildMessage(0, 0, null, false, null, null), e, GovioMessageEntity.Status.RECIPIENT_ALLOWED);
		}

		
		
	}
