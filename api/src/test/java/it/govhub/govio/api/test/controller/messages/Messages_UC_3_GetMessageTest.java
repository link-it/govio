package it.govhub.govio.api.test.controller.messages;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
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
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.repository.MessageRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.MessageUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura messaggi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Messages_UC_3_GetMessageTest {

	private static final String MESSAGES_BASE_PATH = "/v1/messages";
	private static final String MESSAGES_BASE_PATH_DETAIL_ID = MESSAGES_BASE_PATH + "/{id}";

	@Value("${govio.filerepository.path}")
	Path fileRepositoryPath;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	MessageRepository messageRepo;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;

	private DateTimeFormatter dt2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);

	@Value("${govhub.time-zone:Europe/Rome}")
	private String timeZone;

	@Test
	void UC_3_01_GetMessage_NoPlaceholders() throws Exception {
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

		long idMessaggio = response.getInt("id");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID, idMessaggio)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject messaggio = reader.readObject();

		assertNotNull(messaggio.get("id"));
		assertEquals(taxCode, messaggio.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", messaggio.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", messaggio.getString("markdown"));
		assertEquals(email, messaggio.getString("email"));
		assertNotNull(messaggio.get("creation_date"));
		//assertEquals(dt.format(dueDate), messaggio.getString("due_date"));
		assertEquals(GovioMessageEntity.Status.SCHEDULED, GovioMessageEntity.Status.valueOf(response.getString("status")));

		//	assertEquals(dt.format(scheduledExpeditionDate), messaggio.getString("scheduled_expedition_date"));
		JsonObject payment = messaggio.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(invalidAfterDueDate, payment.getBoolean("invalid_after_due_date"));
		assertEquals(noticeNumber, payment.getString("notice_number"));
		assertEquals(payEETaxCode, payment.getString("payee_taxcode"));

		verificaDettaglioDB(idMessaggio, messaggio, payment, false);

	}

	private void verificaDettaglioDB(long idMessaggio, JsonObject messaggio, JsonObject payment, boolean onlyRequired) {
		GovioMessageEntity govioMessageEntity = this.messageRepo.findById(idMessaggio).get();

		assertEquals(govioMessageEntity.getTaxcode(), messaggio.getString("taxcode"));
		assertEquals(dt.format(govioMessageEntity.getScheduledExpeditionDate()), messaggio.getString("scheduled_expedition_date"));

		if(!onlyRequired) {
			assertEquals(govioMessageEntity.getSubject(), messaggio.getString("subject"));
			assertEquals(govioMessageEntity.getMarkdown(), messaggio.getString("markdown"));
			assertEquals(govioMessageEntity.getEmail(), messaggio.getString("email"));
			//assertEquals(govioMessageEntity.getAppioMessageId(), messaggio.getString("appio_message_id"));
			assertEquals(dt.format(govioMessageEntity.getCreationDate()), messaggio.getString("creation_date"));
			// assertEquals(dt.format(govioMessageEntity.getExpeditionDate()), messaggio.getString("expedition_date"));
			assertEquals(dt.format(govioMessageEntity.getDueDate()), messaggio.getString("due_date"));
			assertEquals(dt.format(govioMessageEntity.getLastUpdateStatus()), messaggio.getString("last_update_status"));
			assertEquals(govioMessageEntity.getStatus(), GovioMessageEntity.Status.valueOf( messaggio.getString("status")));
		}
		
		if(payment != null) {
			assertEquals(govioMessageEntity.getAmount(), payment.getJsonNumber("amount").longValueExact());
			assertEquals(govioMessageEntity.getNoticeNumber(), payment.getString("notice_number"));

			if(!onlyRequired) {
				assertEquals(govioMessageEntity.getInvalidAfterDueDate(), payment.getBoolean("invalid_after_due_date"));
				assertEquals(govioMessageEntity.getPayeeTaxcode(), payment.getString("payee_taxcode"));
			} else {
				JsonValue payee_taxcode = payment.get("payee_taxcode");
				assertNull(payee_taxcode);

				JsonValue invalid_after_due_date = payment.get("invalid_after_due_date");
				assertNull(invalid_after_due_date);
			}
		} else {
			assertNull(govioMessageEntity.getAmount());
			assertNull(govioMessageEntity.getInvalidAfterDueDate());
			assertNull(govioMessageEntity.getNoticeNumber());
			assertNull(govioMessageEntity.getPayeeTaxcode());
		}
	}

	@Test
	void UC_3_02_GetMessage_NoPayment() throws Exception {
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

		long idMessaggio = response.getInt("id");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID, idMessaggio)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject messaggio = reader.readObject();

		assertNotNull(messaggio.get("id"));
		assertEquals(taxCode, messaggio.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", messaggio.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", messaggio.getString("markdown"));
		assertEquals(email, messaggio.getString("email"));
		assertNotNull(messaggio.get("creation_date"));
		//assertEquals(dt.format(dueDate), messaggio.getString("due_date"));
		assertEquals(GovioMessageEntity.Status.SCHEDULED, GovioMessageEntity.Status.valueOf(response.getString("status")));
		//	assertEquals(dt.format(scheduledExpeditionDate), messaggio.getString("scheduled_expedition_date"));
		JsonObject payment = messaggio.getJsonObject("payment");
		assertNull(payment);

		verificaDettaglioDB(idMessaggio, messaggio, payment, false);

	}

	@Test
	void UC_3_03_GetMessage_WithPlaceholders() throws Exception {
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

		long idMessaggio = response.getInt("id");

		Map<String, String> placeholdersToCheck = new HashMap<>();
		placeholdersToCheck.put(name, value);
		placeholdersToCheck.put("due_date", this.dt2.format(dueDate)); 

		String subject = MessageUtils.applyPlaceHolders(Costanti.TEMPLATE_CIE_SUBJECT, placeholdersToCheck);
		String markdown = MessageUtils.applyPlaceHolders(Costanti.TEMPLATE_CIE_MESSAGE_BODY, placeholdersToCheck);


		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID, idMessaggio)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject messaggio = reader.readObject();

		assertNotNull(messaggio.get("id"));
		assertEquals(taxCode, messaggio.getString("taxcode"));
		assertEquals(subject, messaggio.getString("subject"));
		assertEquals(markdown, messaggio.getString("markdown"));
		assertEquals(email, messaggio.getString("email"));
		assertNotNull(messaggio.get("creation_date"));
		//assertEquals(dt.format(dueDate), messaggio.getString("due_date"));
		assertEquals(GovioMessageEntity.Status.SCHEDULED, GovioMessageEntity.Status.valueOf(response.getString("status")));
		//	assertEquals(dt.format(scheduledExpeditionDate), messaggio.getString("scheduled_expedition_date"));
		JsonObject payment = messaggio.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(invalidAfterDueDate, payment.getBoolean("invalid_after_due_date"));
		assertEquals(noticeNumber, payment.getString("notice_number"));
		assertEquals(payEETaxCode, payment.getString("payee_taxcode"));

		verificaDettaglioDB(idMessaggio, messaggio, payment, false);
	}

	@Test
	void UC_3_04_GetMessage_OnlyRequired() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = null;

		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, null, taxCode, email, null, null, this.dt);

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

		long idMessaggio = response.getInt("id");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID, idMessaggio)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject messaggio = reader.readObject();

		assertNotNull(response.get("id"));
		assertEquals(taxCode, response.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", response.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", response.getString("markdown"));
		
		JsonValue emailJson = response.get("email");
		assertNull(emailJson);
		
		assertNotNull(response.get("creation_date"));
		
		JsonValue dueDateJson = response.get("due_date");
		assertNull(dueDateJson);
		
		assertEquals(GovioMessageEntity.Status.SCHEDULED, GovioMessageEntity.Status.valueOf(response.getString("status")));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNull(payment);

		verificaDettaglioDB(idMessaggio, messaggio, payment, true);

	}

	@Test
	void UC_3_05_GetMessage_PaymentOnlyRequired() throws Exception {
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

		long idMessaggio = response.getInt("id");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID, idMessaggio)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject messaggio = reader.readObject();

		assertNotNull(messaggio.get("id"));
		assertEquals(taxCode, messaggio.getString("taxcode"));
		assertEquals("Lorem ipsum dolor sit amet.", messaggio.getString("subject"));
		assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", messaggio.getString("markdown"));
		assertEquals(email, messaggio.getString("email"));
		assertNotNull(messaggio.get("creation_date"));
		//assertEquals(dt.format(dueDate), messaggio.getString("due_date"));
		assertEquals(GovioMessageEntity.Status.SCHEDULED, GovioMessageEntity.Status.valueOf(response.getString("status")));
		//	assertEquals(dt.format(scheduledExpeditionDate), messaggio.getString("scheduled_expedition_date"));
		JsonObject payment = messaggio.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(noticeNumber, payment.getString("notice_number"));

		JsonValue payee_taxcode = payment.get("payee_taxcode");
		assertNull(payee_taxcode);

		JsonValue invalid_after_due_date = payment.get("invalid_after_due_date");
		assertNull(invalid_after_due_date);

		verificaDettaglioDB(idMessaggio, messaggio, payment, true);

	}

	// 2. getNotFound
	@Test
	void UC_3_02_GetFile_NotFound() throws Exception {
		int idFile = 10000;

		this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.status", is(404)))
		.andExpect(jsonPath("$.title", is("Not Found")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	// 3. getInvalidID
	@Test
	void UC_3_03_GetFile_InvalidID() throws Exception {
		String idFile = "XXX";

		this.mockMvc.perform(get(MESSAGES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

}
