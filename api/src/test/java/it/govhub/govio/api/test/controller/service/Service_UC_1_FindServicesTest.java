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
package it.govhub.govio.api.test.controller.service;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura dei servizi")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Service_UC_1_FindServicesTest {

	private static final String SERVICES_BASE_PATH = "/v1/services";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_4_01_FindAllOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH)
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());
		
		// ordinamento di default ID desc
		
		assertEquals("Servizi Turistici", items.getJsonObject(0).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(1).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(2).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(3).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(4).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(5).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(6).getString("service_name"));		
		assertEquals("Servizio Generico", items.getJsonObject(7).getString("service_name"));
		assertEquals("CIE", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_02_FindAllOk_Limit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "3");
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();
		
		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(3, page.getInt("limit"));
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());
		
		// ordinamento di default ID desc
		
		assertEquals("Servizi Turistici", items.getJsonObject(0).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(1).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(2).getString("service_name"));
		
	}
	
	@Test
	void UC_4_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
	void UC_4_04_FindAllOk_Offset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
//		JsonArray items = userList.getJsonArray("items");
//		assertEquals(7, items.size());
	}
	
	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
	void UC_4_06_FindAllOk_Q() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "CIE");
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		
		JsonObject item1 = items.getJsonObject(0);
		assertEquals("CIE", item1.getString("service_name"));
		assertEquals("Servizio dev", item1.getString("description"));
		
	}
	
	@Test
	void UC_4_09_FindAllOk_SortServiceName() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals("CIE", items.getJsonObject(0).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(1).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(2).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(3).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(4).getString("service_name"));
		assertEquals("Servizio Generico", items.getJsonObject(5).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(6).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(7).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_10_FindAllOk_SortId() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals("CIE", items.getJsonObject(0).getString("service_name"));
		assertEquals("Servizio Generico", items.getJsonObject(1).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(2).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(3).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(4).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(5).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(6).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(7).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_11_FindAllOk_OffsetLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "3");
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());
		
		assertEquals("Portale ZTL", items.getJsonObject(0).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(1).getString("service_name"));
	}
	
	@Test
	void UC_4_12_FindAllOk_SortServiceNameDesc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals("Variazione Residenza", items.getJsonObject(0).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(1).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(2).getString("service_name"));
		assertEquals("Servizio Generico", items.getJsonObject(3).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(4).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(5).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(6).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(7).getString("service_name"));
		assertEquals("CIE", items.getJsonObject(8).getString("service_name"));
	}
	
	@Test
	void UC_4_13_FindAllOk_SortIdDesc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals("Servizi Turistici", items.getJsonObject(0).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(1).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(2).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(3).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(4).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(5).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(6).getString("service_name"));		
		assertEquals("Servizio Generico", items.getJsonObject(7).getString("service_name"));
		assertEquals("CIE", items.getJsonObject(8).getString("service_name"));
		
	}
	
	@Test
	void UC_4_14_FindAllOk_InvalidSortParam() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "XXX");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
	void UC_4_15_FindAllOk_Sort_Unsorted() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT, "unsorted");
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(SERVICES_BASE_PATH).params(params )
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
		assertEquals(9, page.getInt("total"));
		
		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(9, items.size());

		assertEquals("CIE", items.getJsonObject(0).getString("service_name"));
		assertEquals("Servizio Generico", items.getJsonObject(1).getString("service_name"));
		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(2).getString("service_name"));
		assertEquals("SUAP-Integrazione", items.getJsonObject(3).getString("service_name"));
		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(4).getString("service_name"));
		assertEquals("TARI", items.getJsonObject(5).getString("service_name"));
		assertEquals("Portale ZTL", items.getJsonObject(6).getString("service_name"));
		assertEquals("Variazione Residenza", items.getJsonObject(7).getString("service_name"));
		assertEquals("Servizi Turistici", items.getJsonObject(8).getString("service_name"));
	}
}
