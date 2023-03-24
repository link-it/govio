package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.GovioFileUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione Template")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_4_CreateTemplateFailsTest {

	private static final String TEMPLATES_BASE_PATH = "/v1/templates";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	ReadServiceRepository serviceRepository;
	
	@Autowired
	ReadOrganizationRepository organizationRepository;
	
	@Autowired
	TemplateRepository templateRepository;
	
	@Autowired
	ServiceInstanceRepository instanceRepo;
	
//	@Test
	void UC_4_01_CreateTemplate_InvalidName() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", Costanti.STRING_256)
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.add("has_due_date", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
//	@Test
	void UC_4_02_CreateTemplate_InvalidDescription() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", Costanti.STRING_256)
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.add("has_due_date", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
//	@Test
	void UC_4_03_CreateTemplate_InvalidSubject() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", Costanti.STRING_256)
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.add("has_due_date", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_4_04_CreateTemplate_MissingSubject() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.add("has_due_date", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	
	@Test
	void UC_4_05_CreateTemplate_MissingBody() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("has_payment", true)
				.add("has_due_date", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_4_06_CreateTemplate_MissingHasPayment() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_due_date", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_4_07_CreateTemplate_MissingHasDueDate() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
}
