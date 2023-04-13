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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.GovioFileUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.api.beans.PatchOp.OpEnum;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di modifica Service Instance")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class ServiceInstance_UC_5_PatchServiceInstanceTest {

	private static final String SERVICE_INSTANCES_BASE_PATH = "/v1/service-instances";
	private static final String SERVICE_INSTANCES_BASE_PATH_DETAIL_ID = SERVICE_INSTANCES_BASE_PATH + "/{id}";

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


	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}

	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}

	@ParameterizedTest
	@ValueSource(strings = {"/service_id","/organization_id","/template_id"})
	void UC_5_01_PatchServiceInstance_RefIdNotFound(String patchField) throws Exception {
		int idUser1 = 10000;
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", idUser1);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@ParameterizedTest
	@ValueSource(strings = {"/service_id","/organization_id","/template_id"})
	void UC_5_02_PatchServiceInstance_RefIdInvalid(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "XXXX");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = {"/service_id","/organization_id","/template_id", "apiKey", "enabled"})
	void UC_5_03_PatchServiceInstance_RemoveMandatoryField(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", patchField)
				.add("value", "");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
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
	@ValueSource(strings = {"/service_id","/organization_id","/template_id", "apiKey", "enabled"})
	void UC_5_04_PatchServiceInstance_EmptyMandatoryField(String patchField) throws Exception {
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "");

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
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
	
	@Test
	void UC_5_05_PatchServiceInstance_ServiceId() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_id")
				.add("value", serviceEntity.getId());

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@Test
	void UC_5_06_PatchServiceInstance_OrganizationId() throws Exception {
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/organization_id")
				.add("value", organizationEntity.getId());

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@Test
	void UC_5_07_PatchServiceInstance_TemplateId() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();

		int id = 1;
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/template_id")
				.add("value", templateEntity.getId());

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.status", is(422)))
		.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
	
	@Test
	void UC_5_08_PatchServiceInstance_ApiKey() throws Exception {
		int id = 1;
		String apiKey2 = GovioFileUtils.createApiKey();
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/apiKey")
				.add("value", apiKey2);

		String json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		
		assertEquals(apiKey2, si.getString("apiKey"));
	}
}
