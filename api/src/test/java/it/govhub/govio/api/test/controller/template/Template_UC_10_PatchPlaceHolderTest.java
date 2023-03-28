package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di modifica Place Holder")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_10_PatchPlaceHolderTest {

	private static final String PLACEHOLDERS_BASE_PATH = "/v1/placeholders";
	private static final String PLACEHOLDERS_BASE_PATH_DETAIL_ID = PLACEHOLDERS_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@ParameterizedTest
//	@ValueSource(strings = {"/example","/name", "/type"})
	@ValueSource(strings = {"/type"})
	void UC_10_01_PatchTemplate_InvalidValue(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", Costanti.STRING_256);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"/example","/name", "/type"})
	void UC_10_02_PatchTemplate_RemoveMandatoryField(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", patchField)
				.add("value", "");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@ParameterizedTest
//	@ValueSource(strings = {"/example","/name", "/type"})
	@ValueSource(strings = {"/type"})
	void UC_10_03_PatchTemplate_EmptyMandatoryField(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"/description","/example","/name"})
	void UC_10_04_PatchTemplate_StringValuesOk(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "Updated");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals("Updated", item.getString(patchField.substring(1)));
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "STRING", "DATE", "DATETIME"})
	void UC_10_05_PatchTemplate_TypeValuesOk(String patchValue) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/type")
				.add("value", patchValue);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals(patchValue, item.getString("type"));
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "XXXX", "1234", "false"})
	void UC_10_06_PatchTemplate_InvalidTypeValues(String patchValue) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/type")
				.add("value", patchValue);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(PLACEHOLDERS_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
}
