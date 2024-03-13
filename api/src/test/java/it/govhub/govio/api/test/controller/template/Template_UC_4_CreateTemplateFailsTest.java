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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.json.Json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

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
	
	@Test
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
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_4_02_CreateTemplate_InvalidSubject() throws Exception {
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
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_4_03_CreateTemplate_MissingSubject() throws Exception {
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
	void UC_4_04_CreateTemplate_MissingBody() throws Exception {
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
	void UC_4_05_CreateTemplate_MissingHasPayment() throws Exception {
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
	void UC_4_06_CreateTemplate_MissingHasDueDate() throws Exception {
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
	
	@Test
	void UC_4_07_CreateTemplate_InvalidHasPayment() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", "XXXX")
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
	void UC_4_08_CreateTemplate_InvalidHasDueDate() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", Costanti.STRING_256)
				.add("has_payment", true)
				.add("has_due_date", "XXX")
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
	void UC_4_09_CreateTemplate_EmptySubject() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "")
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
	void UC_4_10_CreateTemplate_EmptyBody() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoTemplate")
				.add("description", "Template di test")
				.add("subject", "Nuova Notifica di pagamento")
				.add("message_body", "")
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
}
