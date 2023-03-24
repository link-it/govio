package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

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
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione Template")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_3_CreateTemplateTest {

	private static final String TEMPLATES_BASE_PATH = "/v1/templates";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	TemplateRepository templateRepository;
	
	@Autowired
	ServiceInstanceRepository instanceRepo;
	
	@Test
	void UC_3_01_CreateTemplateOk() throws Exception {

		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.add("has_due_date", true)
				.build()
				.toString();
		
		MvcResult result = this.mockMvc.perform(post(TEMPLATES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name", is("NuovoTemplate")))
				.andExpect(jsonPath("$.description", is("Template di test")))
				.andExpect(jsonPath("$.subject", is("Nuova Notifica di pagamento")))
				.andExpect(jsonPath("$.message_body", is(Costanti.STRING_256)))
				.andExpect(jsonPath("$.has_payment", is(true)))
				.andExpect(jsonPath("$.has_due_date", is(true)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		int id = si.getInt("id");
		
		GovioTemplateEntity templateEntity = this.templateRepository.findById((long) id).get();
		
		assertEquals(id, templateEntity.getId());
		assertEquals(si.getString("name"), templateEntity.getName());
		assertEquals(si.getString("description"), templateEntity.getDescription());
		assertEquals(si.getString("subject"), templateEntity.getSubject());
		assertEquals(si.getString("message_body"), templateEntity.getMessageBody());
		assertEquals(si.getBoolean("has_payment"), templateEntity.getHasPayment());
		assertEquals(si.getBoolean("has_due_date"), templateEntity.getHasDueDate());
	}
	
}
