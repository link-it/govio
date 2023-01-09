package it.govhub.govio.api.test.controller.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
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
import it.govhub.govio.api.beans.GovioMessageStatus;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioMessageEntity.Status;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.GovioFileMessageRepository;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.repository.GovioMessageRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di consultazione dei messaggi")

class Messages_UC_5_FindMessagesTest {

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
	void UC_5_01_verifyAllFieldsValue() throws Exception {
		
		GovioMessageEntity msg = GovioMessageEntity.builder()
				.amount(10099l)
				.appioMessageId(UUID.randomUUID().toString())
				.creationDate(OffsetDateTime.now())
				.dueDate(OffsetDateTime.now())
				.email("satoshi@bytc0in.mo")
				.expeditionDate(OffsetDateTime.now())
				.govioServiceInstance(em.getReference(GovioServiceInstanceEntity.class,1l))
				.id(1l)
				.invalidAfterDueDate(true)
				.lastUpdateStatus(OffsetDateTime.now())
				.markdown("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
				.noticeNumber("301000001234512345")
				.payeeTaxcode("01234567890")
				.scheduledExpeditionDate(OffsetDateTime.now())
				.sender(em.getReference(UserEntity.class, 1l))
				.status(Status.PROCESSED)
				.subject("Lorem ipsum dolor sit amet.")
				.taxcode("AAAAAA00A00A000A")
				.build();
		
		govioMessageRepository.save(msg);
		
		
		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject messagesList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = messagesList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));
		
		// Controlli sul messaggio
		JsonArray items = messagesList.getJsonArray("items");
		assertEquals(1, items.size());
		
		JsonObject item = items.getJsonObject(0);
		assertNotNull(item.get("id"));
		assertEquals(msg.getTaxcode(), item.getString("taxcode"));
		assertEquals(msg.getSubject(), item.getString("subject"));
		assertEquals(msg.getMarkdown(), item.getString("markdown"));
		assertEquals(msg.getEmail(), item.getString("email"));
		assertEquals(msg.getAppioMessageId(), item.getString("appio_message_id"));
		assertEquals(msg.getCreationDate(), OffsetDateTime.parse(item.getString("creation_date")));
		assertEquals(msg.getExpeditionDate(), OffsetDateTime.parse(item.getString("expedition_date")));
		assertEquals(msg.getDueDate(), OffsetDateTime.parse(item.getString("due_date")));
		assertEquals(msg.getLastUpdateStatus(), OffsetDateTime.parse(item.getString("last_update_status")));
		assertEquals(GovioMessageStatus.valueOf(msg.getStatus().toString()).toString(), item.getString("status"));


		JsonObject payment = item.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(msg.getAmount(), payment.getJsonNumber("amount").longValue());
		assertEquals(msg.getInvalidAfterDueDate(), payment.getBoolean("invalid_after_due_date"));
		assertEquals(msg.getNoticeNumber(), payment.getString("notice_number"));
		assertEquals(msg.getPayeeTaxcode(), payment.getString("payee_taxcode"));

		JsonObject organization = item.getJsonObject("organization");
		assertNotNull(organization);
		JsonObject sender = item.getJsonObject("user");
		assertNotNull(sender);
		JsonObject service = item.getJsonObject("service");
		assertNotNull(service);
		
	}
	
}