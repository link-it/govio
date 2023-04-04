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
package it.govhub.govio.api.test.controller.serviceInstance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

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
import it.govhub.govio.api.beans.EmbedServiceInstanceEnum;
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura Service Instance")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class ServiceInstance_UC_1_FindServiceInstancesTest {

	private static final String SERVICE_INSTANCES_BASE_PATH = "/v1/service-instances";

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
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}

	@Test
	void UC_4_01_FindAllOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH)
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
	}

	@Test
	void UC_4_02_FindAllOk_Limit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "1");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

	}

	@Test
	void UC_4_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");

		this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		//		JsonArray items = userList.getJsonArray("items");
		//		assertEquals(7, items.size());
	}

	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");

		this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

	}

	// servizio ordina per GovioServiceInstanceEntity_.ORGANIZATION +"."+OrganizationEntity_.LEGAL_NAME, GovioServiceInstanceEntity_.SERVICE + "." + ServiceEntity_.NAME)
	@Test
	void UC_4_07_FindAllOk_SortAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

	}

	@Test
	void UC_4_08_FindAllOk_SortDesc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

	}

	@Test
	void UC_4_09_FindAllOk_OffsetLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "3");
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

	}

	@Test
	void UC_4_10_FindAllOk_EnabledTrue() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ENABLED, "true");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		assertEquals(2, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(2, items.size());

	}
	
	@Test
	void UC_4_11_FindAllOk_EnabledFalse() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ENABLED, "false");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
	}
	
	@Test
	void UC_4_12_FindAllOk_ServiceID() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SERVICE_ID, serviceEntity.getId() + "");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

	}
	
	@Test
	void UC_4_13_FindAllOk_OrganizationID() throws Exception {
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_ID, organizationEntity.getId() + "");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

	}
	
	@Test
	void UC_4_14_FindAllOk_ServiceIDSenzaServiceInstance() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SERVICE_ID, serviceEntity.getId() + "");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
	void UC_4_15_FindAllOk_OrganizationIDSenzaServiceInstance() throws Exception {
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_ORGANIZATION_ID, organizationEntity.getId() + "");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
	void UC_4_16_FindAllOk_EmbedService() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_EMBED, EmbedServiceInstanceEnum.SERVICE.getValue());

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

		JsonObject item1 = items.getJsonObject(0);
		
		JsonObject embedded = item1.getJsonObject("_embedded");
		JsonObject service = embedded.getJsonObject("service");
		
		ServiceEntity servizio = this.serviceRepository.findById((long) service.getInt("id")).get();
		
		assertEquals(servizio.getName(), service.getString("service_name"));
		assertEquals(servizio.getDescription(), service.getString("description"));
		
	}
	
	@Test
	void UC_4_17_FindAllOk_EmbedOrganization() throws Exception {
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_EMBED, EmbedServiceInstanceEnum.ORGANIZATION.getValue());

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

		JsonObject item1 = items.getJsonObject(0);
		
		JsonObject embedded = item1.getJsonObject("_embedded");
		JsonObject organization = embedded.getJsonObject("organization");
		
		OrganizationEntity ente = this.organizationRepository.findById((long) organization.getInt("id")).get();
		
		assertEquals(ente.getLegalName(), organization.getString("legal_name"));
		assertEquals(ente.getTaxCode(), organization.getString("tax_code"));
	}
	
	@Test
	void UC_4_18_FindAllOk_EmbedTemplate() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_EMBED, EmbedServiceInstanceEnum.TEMPLATE.getValue());

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

		JsonObject item1 = items.getJsonObject(0);
		
		JsonObject embedded = item1.getJsonObject("_embedded");
		JsonObject template = embedded.getJsonObject("template");
		
		GovioTemplateEntity govioTemplate = this.templateRepository.findById((long) template.getInt("id")).get();
		
		assertEquals(govioTemplate.getSubject(), template.getString("subject"));
		assertEquals(govioTemplate.getMessageBody(), template.getString("message_body"));
		assertEquals(govioTemplate.getHasPayment(), template.getBoolean("has_payment"));
		assertEquals(govioTemplate.getHasDueDate(), template.getBoolean("has_due_date"));	
	}
	
	@Test
	void UC_4_19_FindAllOk_EmbedAll() throws Exception {
		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).queryParam(Costanti.USERS_QUERY_PARAM_EMBED, EmbedServiceInstanceEnum.ORGANIZATION.getValue(), EmbedServiceInstanceEnum.SERVICE.getValue(), EmbedServiceInstanceEnum.TEMPLATE.getValue() )
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

		JsonObject item1 = items.getJsonObject(0);
		
		JsonObject embedded = item1.getJsonObject("_embedded");
		JsonObject template = embedded.getJsonObject("template");
		
		GovioTemplateEntity govioTemplate = this.templateRepository.findById((long) template.getInt("id")).get();
		
		assertEquals(govioTemplate.getSubject(), template.getString("subject"));
		assertEquals(govioTemplate.getMessageBody(), template.getString("message_body"));
		assertEquals(govioTemplate.getHasPayment(), template.getBoolean("has_payment"));
		assertEquals(govioTemplate.getHasDueDate(), template.getBoolean("has_due_date"));	
		
		JsonObject organization = embedded.getJsonObject("organization");
		
		OrganizationEntity ente = this.organizationRepository.findById((long) organization.getInt("id")).get();
		
		assertEquals(ente.getLegalName(), organization.getString("legal_name"));
		assertEquals(ente.getTaxCode(), organization.getString("tax_code"));
		
		JsonObject service = embedded.getJsonObject("service");
		
		ServiceEntity servizio = this.serviceRepository.findById((long) service.getInt("id")).get();
		
		assertEquals(servizio.getName(), service.getString("service_name"));
		assertEquals(servizio.getDescription(), service.getString("description"));
	}
}
