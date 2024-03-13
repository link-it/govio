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
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di modifica Template")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_5_PatchTemplateTest {

	private static final String TEMPLATES_BASE_PATH = "/v1/templates";
	private static final String TEMPLATES_BASE_PATH_DETAIL_ID = TEMPLATES_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	ServiceInstanceRepository instanceRepo;

	@ParameterizedTest
	@ValueSource(strings = {"/subject","/name"})
	void UC_5_01_PatchTemplate_InvalidValue(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", Costanti.STRING_256);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = {"/has_payment","/has_due_date"})
	void UC_5_02_PatchTemplate_InvalidBooleanValue(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "XXXX");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = {"/subject","/message_body", "has_payment", "has_due_date"})
	void UC_5_03_PatchTemplate_RemoveMandatoryField(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", patchField)
				.add("value", "");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = {"/subject","/message_body", "/has_payment", "/has_due_date"})
	void UC_5_04_PatchTemplate_EmptyMandatoryField(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = {"/description","/subject","/name","/message_body"})
	void UC_5_05_PatchTemplate_StringValuesOk(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "Updated");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = { "/has_payment", "/has_due_date"})
	void UC_5_06_PatchTemplate_BooleanValuesOk(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", false);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(patch(TEMPLATES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals(false, item.getBoolean(patchField.substring(1)));
	}
}
