package it.govio.msgsender.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.step.NewMessageProcessor;
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.MessageContent;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.beans.NewMessageDefaultAddresses;
import it.pagopa.io.v1.api.impl.ApiClient;
import it.pagopa.io.v1.api.beans.Payee;
import it.pagopa.io.v1.api.beans.PaymentData;

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

	/**
	 * Costruisce e inserisce in DB un GovioMessageEntity pronto per la spedizione. I parametri obbligatori sono gia' inclusi.
	 * @param due_date
	 * @param amount
	 * @param noticeNumber
	 * @param invalidAfterDueDate
	 * @param p
	 * @param email
	 * @return
	 * @throws URISyntaxException
	 */
	private GovioMessageEntity buildGovioMessageEntity(boolean due_date, Integer amount, String noticeNumber, boolean invalidAfterDueDate, Payee payee, String email) throws URISyntaxException {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = new GovioMessageBuilder().buildGovioMessageEntity(serviceInstanceEntity.get(), Status.RECIPIENT_ALLOWED, due_date, amount, noticeNumber, invalidAfterDueDate, payee, email);
		govioMessagesRepository.save(message);
		return message;
	}

	/**
	 * Costruisce il model NewMessage previsto nella richiesta a IO corrispondente al GovioMessageEntity in input
	 * @param govioMessageEntity
	 * @return
	 */
	private NewMessage buildExpectedNewMessageRequest(GovioMessageEntity govioMessageEntity) {
		NewMessage newMessage = new NewMessage();

		if(govioMessageEntity.getEmail() != null) {
			NewMessageDefaultAddresses address = new NewMessageDefaultAddresses();
			address.setEmail(govioMessageEntity.getEmail());
			newMessage.setDefaultAddresses(address );
		}
		newMessage.setFiscalCode(govioMessageEntity.getTaxcode());
		MessageContent content = new MessageContent();
		content.setMarkdown(govioMessageEntity.getMarkdown());
		content.setSubject(govioMessageEntity.getSubject());
		if(govioMessageEntity.getDue_date() != null)
		content.setDueDate(new Timestamp(govioMessageEntity.getDue_date().toEpochSecond(ZoneOffset.UTC)));
		if(govioMessageEntity.getNoticeNumber() != null) {
			Assert.assertNotNull(govioMessageEntity.getAmount());
			PaymentData paymentData = new PaymentData();
			paymentData.setAmount(govioMessageEntity.getAmount());
			paymentData.setInvalidAfterDueDate(govioMessageEntity.getInvalidAfterDueDate());
			paymentData.setNoticeNumber(govioMessageEntity.getNoticeNumber());
			if(govioMessageEntity.getPayee() != null) {
				Payee payee = new Payee();
				payee.setFiscalCode(govioMessageEntity.getPayee());
				paymentData.setPayee(payee );
			}
			content.setPaymentData(paymentData);
		}
		newMessage.setContent(content);
		return newMessage;
	}

	/**
	 * Predispone il mock del servizio IO in caso di spedizione con successo 
	 * @param govioMessageEntity 
	 * @throws Exception
	 */
	private void setupRestTemplateMock(GovioMessageEntity govioMessageEntity) throws Exception {
		NewMessage newMessage = buildExpectedNewMessageRequest(govioMessageEntity);

		RequestEntity<NewMessage> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/messages"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", govioMessageEntity.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(newMessage, NewMessage.class);

		CreatedMessage createdMessage = new CreatedMessage();
		createdMessage.setId(UUID.randomUUID().toString());

		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<CreatedMessage>() {})))
		.thenReturn(new ResponseEntity<CreatedMessage>(createdMessage, HttpStatus.CREATED));
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
	}

	/**
	 * Predispone il mock del servizio IO in caso di spedizione con errore 
	 * @param govioMessageEntity 
	 * @param exception
	 * @throws Exception
	 */
	private void setupRestTemplateMock(GovioMessageEntity message, RestClientException exception) throws Exception {
		NewMessage newMessage = buildExpectedNewMessageRequest(message);

		RequestEntity<NewMessage> request = RequestEntity
				.post(new URI("https://api.io.pagopa.it/api/v1/messages"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Ocp-Apim-Subscription-Key", message.getGovioServiceInstance().getApikey())
				.header("User-Agent", "Java-SDK")
				.body(newMessage, NewMessage.class);

		ParameterizedTypeReference.forType(CreatedMessage.class);
		Mockito
		.when(restTemplate.exchange(eq(request), eq(new ParameterizedTypeReference<CreatedMessage>() {})))
		.thenThrow(exception);
		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
	}


	@Test
	@DisplayName("UC2.1: Bad request")
	public void UC2_1_BadRequest () throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.BAD_REQUEST, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.2: Profile not exists")
	public void UC2_2_ProfileNotExists () throws Exception {
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.NOT_FOUND);
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.PROFILE_NOT_EXISTS, processedMessage.getStatus());
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
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.DENIED, processedMessage.getStatus());
	}


	@Test
	@DisplayName("UC2_5_Forbidden")
	public void UC2_5_Forbidden() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.FORBIDDEN);
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.FORBIDDEN, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.6: Messaggio minimale (no avviso, no scadenza, no payee, no email)")
	public void UC2_6_MessaggioMinimale() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		setupRestTemplateMock(buildGovioMessageEntity);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.SENT, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.7: Messaggio con avviso")
	public void UC2_7_MessaggioConAvviso() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 1, "000000000000000000", false, null, null);
		setupRestTemplateMock(buildGovioMessageEntity);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.SENT, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.8: Messaggio con scadenza")
	public void UC2_8_MessaggioConScadenza() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(true, 1, "000000000000000000", false, null, null);
		setupRestTemplateMock(buildGovioMessageEntity);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.SENT, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.9: Messaggio con payee")
	public void UC2_9_MessaggioConpayee() throws Exception {
		Payee p = new Payee().fiscalCode("12345678901");
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 1, "000000000000000000", false, p, null);
		setupRestTemplateMock(buildGovioMessageEntity);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.SENT, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.10: Messaggio con email")
	public void UC2_10_MessaggioConEmail() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, "email@gmail.com");
		setupRestTemplateMock(buildGovioMessageEntity);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.SENT, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.11: Errore 5xx)")
	public void UC2_11_Errore5xx() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		HttpServerErrorException e = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.RECIPIENT_ALLOWED, processedMessage.getStatus());
	}
	
	@Test
	@DisplayName("UC2.12: Errore 4xx non previsto")
	public void UC2_12_Errore4xxNonPrevisto() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		HttpClientErrorException e = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.RECIPIENT_ALLOWED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC2.13: Errore interno")
	public void UC2_13_ErroreNonPrevisto() throws Exception {
		GovioMessageEntity buildGovioMessageEntity = buildGovioMessageEntity(false, 0, null, false, null, null);
		RestClientException e = new RestClientException("Error");
		setupRestTemplateMock(buildGovioMessageEntity, e);
		GovioMessageEntity processedMessage = newMessageProcessor.process(buildGovioMessageEntity);
		assertEquals(GovioMessageEntity.Status.RECIPIENT_ALLOWED, processedMessage.getStatus());
	}


}
