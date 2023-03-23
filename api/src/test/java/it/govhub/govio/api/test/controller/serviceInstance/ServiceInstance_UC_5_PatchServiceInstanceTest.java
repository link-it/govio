package it.govhub.govio.api.test.controller.serviceInstance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

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
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", idUser1);

		json = Json.createArrayBuilder()
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
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "XXXX");

		json = Json.createArrayBuilder()
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
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REMOVE.toString())
				.add("path", patchField)
				.add("value", "");

		json = Json.createArrayBuilder()
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
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", patchField)
				.add("value", "");

		json = Json.createArrayBuilder()
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
		
		serviceEntity = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/service_id")
				.add("value", serviceEntity.getId());

		json = Json.createArrayBuilder()
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
		
		organizationEntity = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/organization_id")
				.add("value", organizationEntity.getId());

		json = Json.createArrayBuilder()
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
		
		templateEntity = this.templateRepository.findById(2l).get();
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/template_id")
				.add("value", templateEntity.getId());

		json = Json.createArrayBuilder()
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
		
		String apiKey2 = GovioFileUtils.createApiKey();
		
		JsonObjectBuilder patchOp = Json.createObjectBuilder()
				.add("op", OpEnum.REPLACE.toString())
				.add("path", "/apiKey")
				.add("value", apiKey2);

		json = Json.createArrayBuilder()
				.add(patchOp)
				.build()
				.toString();

		result = this.mockMvc.perform(patch(SERVICE_INSTANCES_BASE_PATH_DETAIL_ID, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType("application/json-patch+json")
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		si = reader.readObject();
		
		assertEquals(apiKey2, si.getString("apiKey"));
	}
}
