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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.repository.TemplatePlaceholderRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di cancellazione Place Holder")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_14_DeleteTemplatePlaceHolderTest {

	private static final String TEMPLATES_BASE_PATH = "/v1/templates";
	private static final String TEMPLATES_BASE_PATH_DETAIL_ID = TEMPLATES_BASE_PATH + "/{template_id}";
	private static final String PLACEHOLDERS_BASE_PATH = TEMPLATES_BASE_PATH_DETAIL_ID + "/placeholders";
	private static final String PLACEHOLDERS_BASE_PATH_DETAIL_ID = PLACEHOLDERS_BASE_PATH + "/{placeholder_id}";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	PlaceholderRepository placeholderRepository;

	@Autowired
	TemplatePlaceholderRepository templatePlaceholderRepository;

	@Test
	void UC_14_01_DeletePlaceHolderOk() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();

		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById(2l).get();
		long idPlaceHolder = govioPlaceholderEntity.getId();

		String json = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 2)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.placeholder_id").isNumber())
				.andExpect(jsonPath("$.mandatory", is(true)))
				.andExpect(jsonPath("$.position", is(2)))
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		long id = si.getInt("placeholder_id");

		List<GovioTemplatePlaceholderEntity> findAll = this.templatePlaceholderRepository.findAll();

		GovioTemplatePlaceholderEntity govioTemplatePlaceholderEntity = findAll.stream().filter(p -> p.getId().getGovioPlaceholder().longValue() == id).collect(Collectors.toList()).get(0);

		assertEquals(id, govioTemplatePlaceholderEntity.getId().getGovioPlaceholder());
		assertEquals(si.getBoolean("mandatory"), govioTemplatePlaceholderEntity.isMandatory());
		assertEquals(si.getInt("position"), govioTemplatePlaceholderEntity.getPosition());

		// cancello il place holder appena creato

		result = this.mockMvc.perform(delete(PLACEHOLDERS_BASE_PATH_DETAIL_ID, idTemplate1, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
//				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn();

	}


	@Test
	void UC_14_02_DeletePlaceHolderFail_PlaceHolderNotFound() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();

		this.mockMvc.perform(delete(PLACEHOLDERS_BASE_PATH_DETAIL_ID, idTemplate1, 10000)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
	
	@Test
	void UC_14_03_DeletePlaceHolderFail_PlaceHolderInvalid() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();

		this.mockMvc.perform(delete(PLACEHOLDERS_BASE_PATH_DETAIL_ID, idTemplate1, "XXXX")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
	
	@Test
	void UC_14_04_DeletePlaceHolderFail_TemplateNotFound() throws Exception {
		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById(2l).get();
		long idPlaceHolder = govioPlaceholderEntity.getId();

		this.mockMvc.perform(delete(PLACEHOLDERS_BASE_PATH_DETAIL_ID, 10000, idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
	
	@Test
	void UC_14_05_DeletePlaceHolderFail_TemplateInvalid() throws Exception {
		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById(2l).get();
		long idPlaceHolder = govioPlaceholderEntity.getId();

		this.mockMvc.perform(delete(PLACEHOLDERS_BASE_PATH_DETAIL_ID, "XXXX", idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();

	}
}

