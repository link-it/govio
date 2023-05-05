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
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.beans.MessageOrdering;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioMessageEntity.Status;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.MessageRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.MessageUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di consultazione dei messaggi")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class Messages_UC_4_FindMessagesTest {

	private static final String MESSAGES_BASE_PATH = "/v1/messages";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	MessageRepository govioMessageRepository;
	
	@Autowired
	ReadServiceRepository serviceRepository;
	
	@Autowired
	ReadOrganizationRepository organizationRepository;

	@Autowired
	EntityManager em;

	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;

	@Value("${govhub.time-zone:Europe/Rome}")
	private String timeZone;
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}

	@BeforeEach
	void setUp() throws Exception{
		String idempotencyKey = MessageUtils.createIdempotencyKey();
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message1 = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		String json = message1.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.param(Costanti.MESSAGES_QUERY_PARAM_IDEMPOTENCY_KEY, idempotencyKey)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isCreated())
		.andReturn();

		idempotencyKey = MessageUtils.createIdempotencyKey();
		taxCode = "AYCSFK56HUQE969P";
		scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(380).toOffsetDateTime(); 
		JsonObject message2 = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, null, null, this.dt);

		json = message2.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.param(Costanti.MESSAGES_QUERY_PARAM_IDEMPOTENCY_KEY, idempotencyKey)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isCreated())
		.andReturn();

		String name = "cie", value = "CA33333FF";
		JsonObject placeholder1 = MessageUtils.createPlaceHolder(name, value);

		JsonArray placeholders = MessageUtils.createPlaceHolders(placeholder1);

		idempotencyKey = MessageUtils.createIdempotencyKey();
		taxCode = "AYCSFK56HUQE969Q";
		scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(355).toOffsetDateTime(); 
		JsonObject message3 = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, placeholders, this.dt);

		json = message3.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "3")
				.param(Costanti.MESSAGES_QUERY_PARAM_IDEMPOTENCY_KEY, idempotencyKey)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isCreated())
		.andReturn();

	}

	@Test
	void UC_4_01_FindAllOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertNotNull(item1.get("id"));
		assertEquals("AYCSFK56HUQE969Q", item1.getString("taxcode"));
		assertNotNull(item1.get("scheduled_expedition_date"));
		JsonObject payment = item1.getJsonObject("payment");
		assertNotNull(payment);

		JsonObject item2 = items.getJsonObject(1);
		assertNotNull(item2.get("id"));
		assertEquals("AYCSFK56HUQE969P", item2.getString("taxcode"));
		assertNotNull(item2.get("scheduled_expedition_date"));
		payment = item2.getJsonObject("payment");
		assertNull(payment);

		JsonObject item3 = items.getJsonObject(2);
		assertNotNull(item3.get("id"));
		assertEquals("AYCSFK56HUQE969O", item3.getString("taxcode"));
		assertNotNull(item3.get("scheduled_expedition_date"));
		payment = item3.getJsonObject("payment");
		assertNotNull(payment);
	}

	@Test
	void UC_4_02_FindAllOk_SortIDAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, MessageOrdering.ID.toString());
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertEquals("AYCSFK56HUQE969O", item1.getString("taxcode"));

		JsonObject item2 = items.getJsonObject(1);
		assertEquals("AYCSFK56HUQE969P", item2.getString("taxcode"));

		JsonObject item3 = items.getJsonObject(2);
		assertEquals("AYCSFK56HUQE969Q", item3.getString("taxcode"));
	}

	@Test
	void UC_4_03_FindAllOk_SortScheduledExpeditioDateAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, MessageOrdering.SCHEDULED_EXPEDITION_DATE.toString());
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertEquals("AYCSFK56HUQE969Q", item1.getString("taxcode"));

		JsonObject item2 = items.getJsonObject(1);
		assertEquals("AYCSFK56HUQE969O", item2.getString("taxcode"));

		JsonObject item3 = items.getJsonObject(2);
		assertEquals("AYCSFK56HUQE969P", item3.getString("taxcode"));
	}

	@Test
	void UC_4_04_FindAllOk_SortUnsortedAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, MessageOrdering.UNSORTED.toString());
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertEquals("AYCSFK56HUQE969O", item1.getString("taxcode"));

		JsonObject item2 = items.getJsonObject(1);
		assertEquals("AYCSFK56HUQE969P", item2.getString("taxcode"));

		JsonObject item3 = items.getJsonObject(2);
		assertEquals("AYCSFK56HUQE969Q", item3.getString("taxcode"));
	}

	@Test
	void UC_4_05_FindAllOk_Limit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "1");

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(1, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

	}

	@Test
	void UC_4_06_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");

		this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	@Test
	void UC_4_07_FindAllOk_Offset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		//		JsonArray items = userList.getJsonArray("items");
		//		assertEquals(7, items.size());
	}

	@Test
	void UC_4_08_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");

		this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	@Test
	void UC_4_09_FindAllOk_ServiceQ() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SERVICE_Q, "CIE");

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());


		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SERVICE_Q, "TARI");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}

	@Test
	void UC_4_10_FindAllOk_OrganizationQ() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_Q, "Creditore");

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());


		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_Q, "12345678901");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(2, items.size());

		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_Q, "12345678902");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}

	@Test
	void UC_4_11_FindAllOk_ServiceID() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_CIE);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SERVICE_ID, "" + serviceEntity.getId());

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

		serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SERVICE_ID, "" + serviceEntity.getId());

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}

	@Test
	void UC_4_12_FindAllOk_OrganizationID() throws Exception {
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_ID, organizationEntity.getId() + "");

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());

		organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		 
		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_ID, organizationEntity.getId() + "");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	@Test
	void UC_4_13_FindAllOk_TaxCode() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_TAX_CODE, "AYCSFK56HUQE969Q");

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(1, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_TAX_CODE, "XXX");

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}
	
	@Test
	void UC_4_14_FindAllOk_ExpeditionDateFrom() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(300).toOffsetDateTime(); 
		
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_EXPEDITION_DATE_FROM, this.dt.format(scheduledExpeditionDate));

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}
	
	@Test
	void UC_4_15_FindAllOk_ExpeditionDateTo() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(500).toOffsetDateTime(); 
		
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_EXPEDITION_DATE_TO, this.dt.format(scheduledExpeditionDate));

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}
	
	@Test
	void UC_4_16_FindAllOk_ScheduledExpeditionDateFrom() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(300).toOffsetDateTime(); 
		
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SCHEDULED_EXPEDITION_DATE_FROM, this.dt.format(scheduledExpeditionDate));

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(500).toOffsetDateTime(); 
		
		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SCHEDULED_EXPEDITION_DATE_FROM, this.dt.format(scheduledExpeditionDate));

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}
	
	@Test
	void UC_4_17_FindAllOk_ScheduledExpeditionDateTo() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(500).toOffsetDateTime(); 
		
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SCHEDULED_EXPEDITION_DATE_TO, this.dt.format(scheduledExpeditionDate));

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(300).toOffsetDateTime(); 
		
		params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SCHEDULED_EXPEDITION_DATE_TO, this.dt.format(scheduledExpeditionDate));

		result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();

		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());

	}

	@Test
	void UC_4_18_FindAllOk_VerifyAllFieldsValue() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_STATUS, GovioMessageEntity.Status.PROCESSED.toString());
		OffsetDateTime now = ZonedDateTime.now(ZoneId.of(this.timeZone)).toOffsetDateTime();

		GovioMessageEntity msg = GovioMessageEntity.builder()
				.amount(10099l)
				.appioMessageId(UUID.randomUUID().toString())
				.creationDate(now)
				.dueDate(now)
				.email("satoshi@bytc0in.mo")
				.expeditionDate(now)
				.govioServiceInstance(em.getReference(GovioServiceInstanceEntity.class,1l))
				.id(1l)
				.invalidAfterDueDate(true)
				.lastUpdateStatus(now)
				.markdown("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
				.noticeNumber("301000001234512345")
				.payeeTaxcode("01234567890")
				.scheduledExpeditionDate(now)
				.sender(em.getReference(UserEntity.class, 1l))
				.status(Status.PROCESSED)
				.subject("Lorem ipsum dolor sit amet.")
				.taxcode("AAAAAA00A00A000A")
				.build();

		govioMessageRepository.save(msg);


		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
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
		assertEquals(msg.getStatus().toString(), item.getString("status"));
		assertEquals(dt.format(msg.getCreationDate()), item.getString("creation_date"));
		assertEquals(dt.format(msg.getExpeditionDate()), item.getString("expedition_date"));
		assertEquals(dt.format(msg.getDueDate()), item.getString("due_date"));
		assertEquals(dt.format(msg.getLastUpdateStatus()), item.getString("last_update_status"));

		JsonObject payment = item.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(msg.getAmount(), payment.getJsonNumber("amount").longValue());
		assertEquals(msg.getInvalidAfterDueDate(), payment.getBoolean("invalid_after_due_date"));
		assertEquals(msg.getNoticeNumber(), payment.getString("notice_number"));
		assertEquals(msg.getPayeeTaxcode(), payment.getString("payee_taxcode"));
	}

	@Test
	void UC_4_19_FindAllOk_StatusScheduled() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_STATUS, GovioMessageEntity.Status.SCHEDULED.toString());

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertEquals(GovioMessageEntity.Status.SCHEDULED.toString(), item1.getString("status"));

		JsonObject item2 = items.getJsonObject(1);
		assertEquals(GovioMessageEntity.Status.SCHEDULED.toString(), item2.getString("status"));

		JsonObject item3 = items.getJsonObject(2);
		assertEquals(GovioMessageEntity.Status.SCHEDULED.toString(), item3.getString("status"));
	}
	
	@Test
	void UC_4_20_FindAllOk_StatusRejected() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_STATUS, GovioMessageEntity.Status.REJECTED.toString());

		MvcResult result = this.mockMvc.perform(get(MESSAGES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
}
