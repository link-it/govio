/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govio.batch.test.step;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.step.GetMessageProcessor;
import it.govio.batch.test.utils.GovioMessageBuilder;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.MessageStatusValue;
import it.pagopa.io.v1.api.impl.ApiClient;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class UC3_GetMessageServiceTest {

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

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
	}

	@SuppressWarnings("unchecked")
	final ArgumentCaptor<RequestEntity<Void>> captor = ArgumentCaptor.forClass(RequestEntity.class);

	/**
	 * Costruisce e inserisce in DB un GovioMessageEntity spedito.
	 * @param due_date
	 * @param amount
	 * @param noticeNumber
	 * @param invalidAfterDueDate
	 * @param p
	 * @param email
	 * @return
	 * @throws URISyntaxException
	 */
	private GovioMessageEntity buildGovioMessageEntity(Status status) throws URISyntaxException {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		GovioMessageEntity message = new GovioMessageBuilder().buildGovioMessageEntity(serviceInstanceEntity.get(), status, false, null, null, false, null, null);
		govioMessagesRepository.save(message);
		return message;
	}

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

//	private void setupRestTemplateMockFail(GovioMessageEntity message,Status status,RestClientException exception) throws Exception {
//		ExternalMessageResponseWithContent response = new ExternalMessageResponseWithContent();
//		response.setStatus(MessageStatusValue.fromValue(status.toString()));
//		Mockito
//		.when(restTemplate.exchange(captor.capture(), eq(new ParameterizedTypeReference<ExternalMessageResponseWithContent>() {})))
//		.thenThrow(exception);
//		Mockito
//		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
//		return;
//	}




	@Test
	@DisplayName("UC1.1: THROTTLED")
	void UC_3_1_THROTTLED() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.THROTTLED);
		setupRestTemplateMock(govioMessageEntity,Status.PROCESSED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);

		assertEquals(HttpMethod.GET, captor.getValue().getMethod());
		assertEquals(new URI("https://api.io.pagopa.it/api/v1/messages/"+govioMessageEntity.getTaxcode()+"/" + govioMessageEntity.getAppioMessageId()), captor.getValue().getUrl());
		assertEquals(govioMessageEntity.getGovioServiceInstance().getApikey(), captor.getValue().getHeaders().getFirst("Ocp-Apim-Subscription-Key"));
		assertEquals(Status.PROCESSED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.2: SENT")
	void UC_3_2_SENT() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.SENT);
		setupRestTemplateMock(govioMessageEntity,Status.PROCESSED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);

		assertEquals(HttpMethod.GET, captor.getValue().getMethod());
		assertEquals(new URI("https://api.io.pagopa.it/api/v1/messages/"+govioMessageEntity.getTaxcode()+"/" + govioMessageEntity.getAppioMessageId()), captor.getValue().getUrl());
		assertEquals(govioMessageEntity.getGovioServiceInstance().getApikey(), captor.getValue().getHeaders().getFirst("Ocp-Apim-Subscription-Key"));
		assertEquals(Status.PROCESSED, processedMessage.getStatus());
	}

	@Test
	@DisplayName("UC1.3: ACCEPTED")
	void UC_3_3_ACCEPTED() throws Exception {
		GovioMessageEntity govioMessageEntity = buildGovioMessageEntity(Status.ACCEPTED);
		setupRestTemplateMock(govioMessageEntity,Status.PROCESSED);
		GovioMessageEntity processedMessage = getMessageProcessor.process(govioMessageEntity);

		assertEquals(HttpMethod.GET, captor.getValue().getMethod());
		assertEquals(new URI("https://api.io.pagopa.it/api/v1/messages/"+govioMessageEntity.getTaxcode()+"/" + govioMessageEntity.getAppioMessageId()), captor.getValue().getUrl());
		assertEquals(govioMessageEntity.getGovioServiceInstance().getApikey(), captor.getValue().getHeaders().getFirst("Ocp-Apim-Subscription-Key"));
		assertEquals(Status.PROCESSED, processedMessage.getStatus());
	}

//	@Test
//	@DisplayName("UC1.4: UNAUTHORIZED")
//	void UC_1_4_UNAUTHORIZED() throws Exception {
//		eseguiTest(Status.ACCEPTED, HttpStatus.UNAUTHORIZED);
//	}
//
//	@Test
//	@DisplayName("UC1.5: FORBIDDEN")
//	void UC_1_5_FORBIDDEN() throws Exception {
//		eseguiTest(Status.ACCEPTED, HttpStatus.FORBIDDEN);
//	}
//
//	@Test
//	@DisplayName("UC1.6: NOT FOUND")
//	void UC_1_6_NOT_FOUND() throws Exception {
//		eseguiTest(Status.ACCEPTED, HttpStatus.NOT_FOUND);
//	}
}

