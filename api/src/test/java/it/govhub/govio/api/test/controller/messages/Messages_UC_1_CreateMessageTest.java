package it.govhub.govio.api.test.controller.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.MessageUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di caricamento messaggi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Messages_UC_1_CreateMessageTest {
	
	private static final String MESSAGES_BASE_PATH = "/v1/messages";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;
	
	private DateTimeFormatter dt2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);
	
	@Value("${govhub.time-zone:Europe/Rome}")
	private String timeZone;

	@Test
	void UC_1_01_CreateMessage_NoPlaceholders() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";
		
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		
		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";
		
		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);
		
		String json = message.toString();
		
		MvcResult result = this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject response = reader.readObject();
		
		assertNotNull(response.get("id"));
		assertEquals(taxCode, response.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", response.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", response.getString("markdown"));
		assertEquals(email, response.getString("email"));
		assertNotNull(response.get("creation_date"));
		assertEquals(dt.format(dueDate), response.getString("due_date"));
		assertEquals("scheduled", response.getString("status"));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(invalidAfterDueDate, payment.getBoolean("invalid_after_due_date"));
		assertEquals(noticeNumber, payment.getString("notice_number"));
		assertEquals(payEETaxCode, payment.getString("payee_taxcode"));
		
	}
	
	@Test
	void UC_1_02_CreateMessage_NoPayment() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		
		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";
		
		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, null, null, this.dt);
		
		String json = message.toString();
		
		MvcResult result = this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject response = reader.readObject();
		
		assertNotNull(response.get("id"));
		assertEquals(taxCode, response.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", response.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", response.getString("markdown"));
		assertEquals(email, response.getString("email"));
		assertNotNull(response.get("creation_date"));
		assertEquals(dt.format(dueDate), response.getString("due_date"));
		assertEquals("scheduled", response.getString("status"));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNull(payment);
		
	}
	
	@Test
	void UC_1_03_CreateMessage_WithPlaceholders() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";
		
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		
		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";
		
		String name = "cie", value = "CA33333FF";
		JsonObject placeholder1 = MessageUtils.createPlaceHolder(name, value);
		
		JsonArray placeholders = MessageUtils.createPlaceHolders(placeholder1);
		
		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, placeholders, this.dt);
		
		String json = message.toString();
		
		MvcResult result = this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "3")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject response = reader.readObject();
		
		assertNotNull(response.get("id"));
		assertEquals(taxCode, response.getString("taxcode"));
		assertNotNull(response.get("subject"));
		assertNotNull(response.get("markdown"));
		
		Map<String, String> placeholdersToCheck = new HashMap<>();
		placeholdersToCheck.put(name, value);
		placeholdersToCheck.put("due_date", this.dt2.format(dueDate)); 
		
		String subject = MessageUtils.applyPlaceHolders(Costanti.TEMPLATE_CIE_SUBJECT, placeholdersToCheck);
		String markdown = MessageUtils.applyPlaceHolders(Costanti.TEMPLATE_CIE_MESSAGE_BODY, placeholdersToCheck);
		
		assertEquals(subject, response.getString("subject"));
		assertEquals(markdown, response.getString("markdown"));
		
		assertEquals(email, response.getString("email"));
		assertNotNull(response.get("creation_date"));
		assertEquals(dt.format(dueDate), response.getString("due_date"));
		assertEquals("scheduled", response.getString("status"));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(invalidAfterDueDate, payment.getBoolean("invalid_after_due_date"));
		assertEquals(noticeNumber, payment.getString("notice_number"));
		assertEquals(payEETaxCode, payment.getString("payee_taxcode"));
		
	}


	@Test
	void UC_1_04_CreateMessage_OnlyRequired() throws Exception {
		
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		
		String taxCode = "AYCSFK56HUQE969O";
		
		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, null, taxCode, null, null, null, this.dt);
		
		String json = message.toString();
		
		MvcResult result = this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject response = reader.readObject();
		
		assertNotNull(response.get("id"));
		assertEquals(taxCode, response.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", response.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", response.getString("markdown"));
		assertNotNull(response.get("creation_date"));
		assertEquals("scheduled", response.getString("status"));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNull(payment);

	}
	
	@Test
	void UC_1_05_CreateMessage_PaymentOnlyRequired() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = null;
		String payEETaxCode = null;
		
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		
		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";
		
		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);
		
		String json = message.toString();
		
		MvcResult result = this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject response = reader.readObject();
		
		assertNotNull(response.get("id"));
		assertEquals(taxCode, response.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", response.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", response.getString("markdown"));
		assertEquals(email, response.getString("email"));
		assertNotNull(response.get("creation_date"));
		assertEquals(dt.format(dueDate), response.getString("due_date"));
		assertEquals("scheduled", response.getString("status"));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(noticeNumber, payment.getString("notice_number"));
		
		JsonValue payee_taxcode = payment.get("payee_taxcode");
		assertNull(payee_taxcode);
		
		JsonValue invalid_after_due_date = payment.get("invalid_after_due_date");
		assertNull(invalid_after_due_date);
	}
}
