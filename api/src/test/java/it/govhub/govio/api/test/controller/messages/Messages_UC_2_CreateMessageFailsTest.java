package it.govhub.govio.api.test.controller.messages;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.entity.GovioMessageEntity;
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.repository.ServiceInstanceFilters;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.MessageUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di caricamento messaggi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Messages_UC_2_CreateMessageFailsTest {

	private static final String MESSAGES_BASE_PATH = "/v1/messages";
	private static final String PLACEHOLDERS_BASE_PATH = "/v1/placeholders";
	private static final String PLACEHOLDERS_BASE_PATH_DETAIL_ID = PLACEHOLDERS_BASE_PATH + "/{id}";
	private static final String TEMPLATES_BASE_PATH = "/v1/templates";
	private static final String TEMPLATES_BASE_PATH_DETAIL_ID = TEMPLATES_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;
	
	private DateTimeFormatter dt2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY);
	
	@Value("${govhub.time-zone:Europe/Rome}")
	private String timeZone;
	
	@Autowired
	private ReadOrganizationRepository organizationRepository;

	@Autowired
	private ReadServiceRepository serviceRepository;
	
	@Autowired
	private ServiceInstanceRepository serviceInstanceRepository;
	
	@Autowired
	PlaceholderRepository placeholderRepository;

	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}

	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private GovioPlaceholderEntity leggiPlaceHolderDB(String nome) {
		List<GovioPlaceholderEntity> findAll = this.placeholderRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private GovioServiceInstanceEntity leggiServiceInstanceDB(Long idOrganization, Long idService) {

		Specification<GovioServiceInstanceEntity> spec = ServiceInstanceFilters.empty();

		spec = spec.and(ServiceInstanceFilters.byOrganizationIds(Arrays.asList(idOrganization)));
		spec = spec.and(ServiceInstanceFilters.byServiceIds(Arrays.asList(idService)));

		List<GovioServiceInstanceEntity> findAll = this.serviceInstanceRepository.findAll(spec);

		return findAll.size() > 0 ? findAll.get(0) : null;
	}

	/**
	 * Caricamento di un nuovo messaggio senza indicare il campo obbligatorio taxCode.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_01_CreateMessage_MissingTaxCode() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = null; 
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio senza indicare il campo obbligatorio scheduledExpeditionDate.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_02_CreateMessage_MissingScheduledExpeditionDate() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = null; //ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio senza indicare il campo obbligatorio amount.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_03_CreateMessage_MissingAmount() throws Exception {
		Long amount = null;
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

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio senza indicare il campo obbligatorio noticeNumber.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_04_CreateMessage_MissingNoticeNumber() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = null;
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio il campo taxCode viene impostato con valori che non rispettano i criteri previsti:
	 * - minLength 16
	 * - maxLength: 16
	 * - pattern: [A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z] 
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_05_CreateMessage_InvalidTaxCode() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		// 1. minLength: 16
		String taxCode = "XXX"; 
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 2. maxLength: 16	
		taxCode = "XXXXXXXXXXXXXXXXXXX"; 
		message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 3. pattern: [A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]
		taxCode = "1SSMRA50A18X111Z"; 
		message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio il campo email viene impostato con valori che non rispettano i criteri previsti:
	 * - maxLength: 255
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_06_CreateMessage_InvalidEmail() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 

		// 1. maxLength: 255
		String email = Costanti.STRING_256;

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio con placeHolder che non specifica il campo name
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_07_CreateMessage_PlaceHolderNameNotPresent() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		String name = null, value = "CA33333FF";
		JsonObject placeholder1 = MessageUtils.createPlaceHolder(name, value);

		JsonArray placeholders = MessageUtils.createPlaceHolders(placeholder1);

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, placeholders, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio con placeHolder che non specifica il campo value
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_08_CreateMessage_PlaceHolderValueNotPresent() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		String name = "cie", value = null;
		JsonObject placeholder1 = MessageUtils.createPlaceHolder(name, value);

		JsonArray placeholders = MessageUtils.createPlaceHolders(placeholder1);

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, placeholders, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio il campo di tipo date scheduledExpeditionDate contiene una data non valida
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_09_CreateMessage_InvalidScheduled_expedition_date() throws Exception {
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createInvalidDateMessage("XXXX", this.dt.format(dueDate), taxCode, email, null, null);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio il campo di tipo date dueDate contiene una data non valida
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_10_CreateMessage_InvalidDue_date() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createInvalidDateMessage(this.dt.format(scheduledExpeditionDate), "XXXX",  taxCode, email, null, null);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio il campo amount viene impostato con valori che non rispettano i criteri previsti:
	 * - stringa errata XXXX
	 * - minimum: 1
	 * - maximum: 9999999999
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_11_CreateMessage_PaymentInvalidAmount() throws Exception {
		// 1. valore non numerico
		String amount = "XXXX";
		String noticeNumber = "159981576728496290";
		String invalidAfterDueDate = "true";
		String payEETaxCode = "50751457039";
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 

		String email = "s.nakamoto@xxxxx.xx";

		JsonObject payment = MessageUtils.createInvalidPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 2.	minimum: 1
		amount = "0";
		payment = MessageUtils.createInvalidPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);
		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 3.	maximum: 9999999999
		amount = "1000000000000";
		payment = MessageUtils.createInvalidPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);
		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio il campo di tipo boolean invalidAfterDueDate contiene una valore non valido
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_12_CreateMessage_PaymentInvalid_invalid_after_due_date() throws Exception {
		String amount = "9999999999";
		String noticeNumber = "159981576728496290";
		String invalidAfterDueDate = "XXX";
		String payEETaxCode = "50751457039";
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 

		String email = "s.nakamoto@xxxxx.xx";

		JsonObject payment = MessageUtils.createInvalidPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio il campo noticeNumber viene impostato con valori che non rispettano i criteri previsti:
	 * - pattern: ^[0123][0-9]{17}$
	 * - maxlength 18
	 * - minlength 18
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_13_CreateMessage_PaymentInvalidNoticeNumber() throws Exception {
		Long amount = 9999999999L;
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		// 1.pattern: ^[0123][0-9]{17}$
		String noticeNumber = "12345678901234567A";

		JsonObject payment = MessageUtils.createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 2. maxlength 18
		noticeNumber = "1234567890123456789";

		payment = MessageUtils.createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 2. minlength 18
		noticeNumber = "12345678901234567";

		payment = MessageUtils.createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio il campo payEETaxCode viene impostato con valori che non rispettano i criteri previsti:
	 * - pattern: [0-9]{11}
	 * - maxlength 11
	 * - minlength 11
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_14_CreateMessage_PaymentInvalidPayeeTaxcode() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		// 1. pattern: [0-9]{11}
		String payEETaxCode = "A0751457039";

		JsonObject payment = MessageUtils.createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 2. maxLength: 11
		payEETaxCode = "123456789012";

		payment = MessageUtils.createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();

		// 2.minLength: 11
		payEETaxCode = "1234567890";

		payment = MessageUtils.createPaymentObject(amount, noticeNumber, invalidAfterDueDate, payEETaxCode);
		message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, payment, null, this.dt);

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "1")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	}

	/**
	 * Caricamento di un nuovo messaggio per un serviceIstance non registrato
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_15_CreateMessage_ServiceID_NonRegistrato() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, null, null, this.dt);

		String json = message.toString();

		int idNonPresente = 10000;
		
		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance",  idNonPresente +"")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

	}
	
	/**
	 * Caricamento di un nuovo messaggio senza indicare un serviceInstance nella richiesta
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_16_CreateMessage_Parametro_ServiceIDNonPresente() throws Exception {
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, null, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

	}

	/**
	 * Caricamento di un nuovo messaggio con verifica dell'applicazione dei placeholder
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_17_CreateMessage_AuthorizedSI() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_CIE_ORG);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_CIE);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");
		
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
				.params(params)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAutorizzataSI())
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
		assertEquals(GovioMessageEntity.Status.SCHEDULED, GovioMessageEntity.Status.valueOf(response.getString("status")));
		assertEquals(dt.format(scheduledExpeditionDate), response.getString("scheduled_expedition_date"));
		JsonObject payment = response.getJsonObject("payment");
		assertNotNull(payment);
		assertEquals(amount,payment.getJsonNumber("amount").longValueExact());
		assertEquals(invalidAfterDueDate, payment.getBoolean("invalid_after_due_date"));
		assertEquals(noticeNumber, payment.getString("notice_number"));
		assertEquals(payEETaxCode, payment.getString("payee_taxcode"));
	}

	/**
	 * Caricamento di un nuovo messaggio per un serviceIstance a cui l'utenza non e' autorizzata.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_18_CreateMessage_OrganizationNotAuthorized() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");
		
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, null, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.params(params)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAutorizzataSI())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
		.andExpect(status().isUnauthorized())
		.andReturn();


	}

	/**
	 * Caricamento di un nuovo messaggio per un serviceIstance disabilitato.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_19_CreateMessage_ServiceInstanceDisabled() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_IMU);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");
		
		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O";
		String email = "s.nakamoto@xxxxx.xx";

		JsonObject message = MessageUtils.createMessage(scheduledExpeditionDate, dueDate, taxCode, email, null, null, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.params(params)
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();


	}
	
	/**
	 * Caricamento di un nuovo messaggio senza indicare i placeholders per un template che li richiede. 
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_20_CreateMessage_PlaceHoldersRequired() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		JsonArray placeholders = null;

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, placeholders, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "3")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	/**
	 * Caricamento di un nuovo messaggio senza indicare la dueDate per un template che la richiede. 
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_21_CreateMessage_MissingDueDate() throws Exception {
		Long amount = 9999999999L;
		String noticeNumber = "159981576728496290";
		Boolean invalidAfterDueDate = true;
		String payEETaxCode = "50751457039";

		OffsetDateTime scheduledExpeditionDate = ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 
		OffsetDateTime dueDate = null; //ZonedDateTime.now(ZoneId.of(this.timeZone)).plusDays(365).toOffsetDateTime(); 

		String taxCode = "AYCSFK56HUQE969O"; 
		String email = "s.nakamoto@xxxxx.xx";

		String name = "cie", value = "CA33333FF";
		JsonObject placeholder1 = MessageUtils.createPlaceHolder(name, value);
		
		JsonArray placeholders = MessageUtils.createPlaceHolders(placeholder1);

		JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
				dueDate, taxCode, email, placeholders, this.dt);

		String json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "3")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail", is("Il valore per il placeholder due_date è obbligatorio")))
		.andReturn();
	}
	
	/**
	 * Caricamento di un nuovo messaggio indicando un placeholders di un tipo non valido per il template. 
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_22_CreateMessage_BadPlaceHolderType() throws Exception {
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
		
		GovioPlaceholderEntity placeholderEntity = leggiPlaceHolderDB(name);
		
		// modifico il tipo del placeholder cie
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/type")
				.add("value", "DATETIME");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, placeholderEntity.getId())
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		

		try {
			JsonObject message = MessageUtils.createMessage(amount, noticeNumber, invalidAfterDueDate, payEETaxCode, scheduledExpeditionDate,
					dueDate, taxCode, email, placeholders, this.dt);

			json = message.toString();
			
			this.mockMvc.perform(
					post(MESSAGES_BASE_PATH)
					.param("service_instance", "3")
					.content(json)
					.contentType(MediaType.APPLICATION_JSON)
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnprocessableEntity())
			.andExpect(jsonPath("$.status", is(422)))
			.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
			.andExpect(jsonPath("$.type").isString())
			.andExpect(jsonPath("$.detail", is("Il valore "+value+" del placeholder cie non presenta una data con ora valida.")))
			.andReturn();
		} finally {
			JsonObjectBuilder patchOpReverse = Json.createObjectBuilder()
					.add("op", OpEnum.REPLACE.toString())
					.add("path", "/type")
					.add("value", "STRING");
			json = Json.createArrayBuilder()
					.add(patchOpReverse)
					.build()
					.toString();
		
			this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, placeholderEntity.getId())
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.content(json)
					.contentType("application/json-patch+json")
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();
		}
	}
	
	/**
	 * Caricamento di un nuovo messaggio dove il template del subject e' errato.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_23_CreateMessage_BadSubjectFreeMarker() throws Exception {
		int idTemplate = 2;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/subject")
				.add("value", "<#if wrongTemplate>");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, idTemplate)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
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

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "3")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isInternalServerError())
		.andExpect(jsonPath("$.status", is(500)))
		.andExpect(jsonPath("$.title", is("Internal Server Error")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail", startsWith("Errore nell'applicazione del freemarker per l'oggetto del messaggio")))
		.andReturn();
	}
	
	/**
	 * Caricamento di un nuovo messaggio dove il template del body e' errato.
	 * 
	 * @throws Exception
	 */
	@Test
	void UC_2_24_CreateMessage_BadMessageBodyFreeMarker() throws Exception {
		int idTemplate = 2;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/message_body")
				.add("value", "<#if wrongTemplate>");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, idTemplate)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
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

		json = message.toString();

		this.mockMvc.perform(
				post(MESSAGES_BASE_PATH)
				.param("service_instance", "3")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isInternalServerError())
		.andExpect(jsonPath("$.status", is(500)))
		.andExpect(jsonPath("$.title", is("Internal Server Error")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail", startsWith("Errore nell'applicazione del freemarker per il contenuto del messaggio")))
		.andReturn();
	}
}
