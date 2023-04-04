/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
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
import it.govhub.govio.api.beans.EmbedPlaceholderEnum;
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura PlaceHolder di un Template")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_11_FindTemplatePlaceHoldersTest {

	private static final String TEMPLATES_BASE_PATH = "/v1/templates";
	private static final String TEMPLATES_BASE_PATH_DETAIL_ID = TEMPLATES_BASE_PATH + "/{id}";
	private static final String PLACEHOLDERS_BASE_PATH = TEMPLATES_BASE_PATH_DETAIL_ID + "/placeholders";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	TemplateRepository templateRepository;
	
	@Autowired
	PlaceholderRepository placeholderRepository;
	
	@Test
	void UC_11_01_FindAllOk() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();
		
		MvcResult result = this.mockMvc.perform(get(PLACEHOLDERS_BASE_PATH, idTemplate1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertEquals(1, item1.getInt("placeholder_id"));
		assertEquals(1, item1.getInt("position"));
		assertEquals(true, item1.getBoolean("mandatory"));
		
	}

	@Test
	void UC_11_02_FindAllOk_Embed() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();
		
		MvcResult result = this.mockMvc.perform(get(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_EMBED, EmbedPlaceholderEnum.PLACEHOLDER.getValue(), EmbedPlaceholderEnum.TEMPLATE.getValue() )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

		JsonObject item1 = items.getJsonObject(0);
		assertEquals(1, item1.getInt("placeholder_id"));
		assertEquals(1, item1.getInt("position"));
		assertEquals(true, item1.getBoolean("mandatory"));
		
		JsonObject embedded = item1.getJsonObject("_embedded");
		
		JsonObject template = embedded.getJsonObject("template");
		
		GovioTemplateEntity govioTemplate = this.templateRepository.findById((long) template.getInt("id")).get();
		
		assertEquals(govioTemplate.getSubject(), template.getString("subject"));
		assertEquals(govioTemplate.getMessageBody(), template.getString("message_body"));
		assertEquals(govioTemplate.getHasPayment(), template.getBoolean("has_payment"));
		assertEquals(govioTemplate.getHasDueDate(), template.getBoolean("has_due_date"));	
		
		JsonObject placeholder = embedded.getJsonObject("placeholder");
		
		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById((long) placeholder.getInt("id")).get();
		
		assertEquals(govioPlaceholderEntity.getName(), placeholder.getString("name"));
		assertEquals(govioPlaceholderEntity.getType().toString(), placeholder.getString("type"));
		assertEquals(govioPlaceholderEntity.getExample(), placeholder.getString("example"));
		assertEquals(govioPlaceholderEntity.getDescription(), placeholder.getString("description"));
		assertNull(govioPlaceholderEntity.getPattern());
	}
}
