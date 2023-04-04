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
package it.govhub.govio.api.test.controller.organization;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

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
import it.govhub.govio.api.config.GovioRoles;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura organization")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Organization_UC_1_FindOrganizationsTest {

	private static final String ORGANIZATIONS_BASE_PATH = "/v1/organizations";

	@Value("${govio.filerepository.path}")
	Path fileRepositoryPath;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_1_01_FindAllOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH)
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(4, items.size());
		
		// ordinamento di default ID desc
		
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_3, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_2, items.getJsonObject(1).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(2).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(3).getString("tax_code"));
	}
	
	@Test
	void UC_1_02_FindAllOk_Limit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "1");
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());
		
		// ordinamento di default ID desc
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_3, items.getJsonObject(0).getString("tax_code"));
	}
	
	@Test
	void UC_1_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
	void UC_1_04_FindAllOk_Offset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
//		JsonArray items = userList.getJsonArray("items");
//		assertEquals(7, items.size());
//		
////		assertEquals("amministratore", items.getJsonObject(0).getString("principal"));
//		assertEquals("ospite", items.getJsonObject(0).getString("principal"));
//		assertEquals("user_viewer", items.getJsonObject(1).getString("principal"));
//		assertEquals("user_editor", items.getJsonObject(2).getString("principal"));
//		assertEquals("org_viewer", items.getJsonObject(3).getString("principal"));
//		assertEquals("org_editor", items.getJsonObject(4).getString("principal"));
//		assertEquals(user.getPrincipal(), items.getJsonObject(5).getString("principal"));
//		assertEquals(user2.getPrincipal(), items.getJsonObject(6).getString("principal"));
	}
	
	@Test
	void UC_1_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
	void UC_1_06_FindAllOk_Q() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "naka");
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
	void UC_1_07_FindAllOk_OffsetLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "2");
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(2, page.getInt("offset"));	
		assertEquals(2, page.getInt("limit"));
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(1).getString("tax_code"));
	}
	
	@Test
	void UC_1_08_FindAllOk_SortLegalnameDesc() throws Exception {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "legal_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(4, items.size());

		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_3, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_2, items.getJsonObject(1).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(2).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(3).getString("tax_code"));
	}
	
	@Test
	void UC_1_09_FindAllOk_SortIdDesc() throws Exception {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(4, items.size());

		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_3, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_2, items.getJsonObject(1).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(2).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(3).getString("tax_code"));
	}
	
	@Test
	void UC_1_10_FindAllOk_InvalidSortParam() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "XXX");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
	void UC_1_11_FindAllOk_Sort_Unsorted() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "unsorted");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(4, items.size());

		
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(1).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_2, items.getJsonObject(2).getString("tax_code"));
	}

	@Test
	void UC_1_12_FindAllOk_WithRoleGovio_SysAdmin() throws Exception {
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).queryParam(Costanti.USERS_QUERY_PARAM_ROLES, GovioRoles.GOVIO_SYSADMIN )
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
		assertEquals(4, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(4, items.size());
		
		// ordinamento di default ID desc
		
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_3, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_2, items.getJsonObject(1).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(2).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(3).getString("tax_code"));
	}
	
	@Test
	void UC_1_13_FindAllOk_WithRoleGovio_Viewer() throws Exception {
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).queryParam(Costanti.USERS_QUERY_PARAM_ROLES, GovioRoles.GOVIO_VIEWER )
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
	
//	@Test
	void UC_1_14_FindAllOk_WithServiceInstanceTrue() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_WITH_SERVICE_INSTANCE, "true");
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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

		
		
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_2, items.getJsonObject(0).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE, items.getJsonObject(1).getString("tax_code"));
		assertEquals(Costanti.TAX_CODE_CIE_ORG, items.getJsonObject(2).getString("tax_code"));
	}
	
//	@Test
	void UC_1_15_FindAllOk_WithServiceInstanceFalse() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_WITH_SERVICE_INSTANCE, "false");
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH).params(params )
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

		assertEquals(Costanti.TAX_CODE_ENTE_CREDITORE_3, items.getJsonObject(0).getString("tax_code"));
	}
}
