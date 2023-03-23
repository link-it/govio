package it.govhub.govio.api.test.controller.serviceInstance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
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

class ServiceInstance_UC_3_CreateServiceInstanceTest {

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
	void UC_3_01_CreateServiceInstanceOk() throws Exception {
		ServiceEntity serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		OrganizationEntity organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		
		String apiKey = GovioFileUtils.createApiKey();
		String json = Json.createObjectBuilder()
				.add("service_id", serviceEntity.getId())
				.add("organization_id", organizationEntity.getId())
				.add("template_id", templateEntity.getId())
				.add("apiKey", apiKey)
				.add("enabled", true)
				.build()
				.toString();
		
		MvcResult result = this.mockMvc.perform(post(SERVICE_INSTANCES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.apiKey", is(apiKey)))
				.andExpect(jsonPath("$.enabled", is(true)))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		int id = si.getInt("id");
		
		assertEquals(si.getInt("organization_id"), organizationEntity.getId());
		assertEquals(si.getInt("service_id"), serviceEntity.getId());
		assertEquals(si.getInt("template_id"), templateEntity.getId());
		
		GovioServiceInstanceEntity govioServiceInstanceEntity = this.instanceRepo.findById((long) id).get();
		
		assertEquals(id, govioServiceInstanceEntity.getId());
		assertEquals(si.getString("apiKey"), govioServiceInstanceEntity.getApiKey());
		assertEquals(si.getBoolean("enabled"), govioServiceInstanceEntity.getEnabled());
		assertEquals(organizationEntity.getId(), govioServiceInstanceEntity.getOrganization().getId());
		assertEquals(serviceEntity.getId(), govioServiceInstanceEntity.getService().getId());
		assertEquals(templateEntity.getId(), govioServiceInstanceEntity.getTemplate().getId());
	}
	
}
