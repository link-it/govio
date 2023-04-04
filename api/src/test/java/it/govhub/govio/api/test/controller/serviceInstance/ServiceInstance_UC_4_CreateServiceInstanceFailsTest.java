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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

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
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.GovioFileUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione Service Instance")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class ServiceInstance_UC_4_CreateServiceInstanceFailsTest {

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
	
	@Test
	void UC_4_01_CreateServiceInstance_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", serviceEntity.getId())
				.add("organization_id", idUser1)
				.add("template_id", templateEntity.getId())
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_4_02_CreateServiceInstance_ServiceNotFound() throws Exception {
		int idUser1 = 10000;
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", idUser1)
				.add("organization_id", organizationEntity.getId())
				.add("template_id", templateEntity.getId())
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_4_03_CreateServiceInstance_TemplateNotFound() throws Exception {
		int idUser1 = 10000;
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", serviceEntity.getId())
				.add("organization_id", organizationEntity.getId())
				.add("template_id", idUser1)
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_4_04_CreateServiceInstance_MissingOrganization() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", serviceEntity.getId())
				.add("template_id", templateEntity.getId())
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
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
	void UC_4_05_CreateServiceInstance_MissingService() throws Exception {
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("organization_id", organizationEntity.getId())
				.add("template_id", templateEntity.getId())
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
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
	void UC_4_06_CreateServiceInstance_MissingTemplate() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", serviceEntity.getId())
				.add("organization_id", organizationEntity.getId())
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
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
	void UC_4_07_CreateServiceInstance_MissingApiKey() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		
//		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", serviceEntity.getId())
				.add("organization_id", organizationEntity.getId())
				.add("template_id", templateEntity.getId())
				.add("enabled", true)
				.build()
				.toString();
		
		this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
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
