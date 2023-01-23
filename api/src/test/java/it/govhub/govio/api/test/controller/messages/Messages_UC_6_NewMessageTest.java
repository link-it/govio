package it.govhub.govio.api.test.controller.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.repository.GovioFileMessageRepository;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione messaggi")

class Messages_UC_6_NewMessageTest {

	private static final String MESSAGES_BASE_PATH = "/v1/messages";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	GovioMessageRepository govioMessageRepository;

	@Autowired
	private GovioFileRepository govioFilesRepository;

	@Autowired
	private GovioFileMessageRepository govioFileMessagesRepository;

	@Autowired
	EntityManager em;

	@BeforeEach
	void emptyGovioMessages() {
		govioFilesRepository.deleteAll();
		govioFileMessagesRepository.deleteAll();
		govioMessageRepository.deleteAll();
	}

	@Test
	void UC_5_01_createMessage() throws Exception {

		StringBuffer request = new StringBuffer();
		request.append("{");
		request.append("\"taxcode\": \"AYCSFK56HUQE969O\",");
		request.append("\"payment\": {");
		request.append("  \"amount\": 9999999999,");
		request.append("  \"notice_number\": \"159981576728496290\",");
		request.append("  \"invalid_after_due_date\": true,");
		request.append("  \"payee_taxcode\": \"50751457039\"");
		request.append("},");
		request.append("\"email\": \"s.nakamoto@xxxxx.xx\",");
		request.append("\"scheduled_expedition_date\": \"2050-06-01T12:00:00+02:00\",");
		request.append("\"due_date\": \"2050-12-31T12:00:00+01:00\"");
		request.append("}");

		MvcResult result = this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(request.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject message = reader.readObject();

		assertNotNull(message.get("id"));
		assertEquals("AYCSFK56HUQE969O", message.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", message.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", message.getString("markdown"));
		assertEquals("s.nakamoto@xxxxx.xx", message.getString("email"));
		assertNotNull(message.get("creation_date"));
		assertEquals("2050-12-31T12:00:00+01:00", message.getString("due_date"));
		assertEquals("scheduled", message.getString("status"));
		assertEquals("2050-06-01T12:00:00+02:00", message.getString("scheduled_expedition_date"));
		JsonObject payment = message.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(9999999999l,payment.getJsonNumber("amount").longValueExact());
		assertEquals(true, payment.getBoolean("invalid_after_due_date"));
		assertEquals("159981576728496290", payment.getString("notice_number"));
		assertEquals("50751457039", payment.getString("payee_taxcode"));

	}

}
