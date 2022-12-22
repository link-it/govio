package it.govhub.govio.api.test.controller.trace;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.repository.OrganizationRepository;
import it.govhub.govregistry.commons.repository.ServiceRepository;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di caricamento csv tracciati")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Trace_UC_1_UploadCsvTracesTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private ServiceRepository serviceRepository;
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	// 1. Upload OK file csv per utenza admin 
	@Test
	void UC_1_01_UploadCsvFileOk_Utenza_Admin() throws Exception {
		String fileName = "csv-test-UC101";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		//JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
	}
	
	// 2. Upload OK file csv per utenza con ruolo govio_sender
	@Test
	void UC_1_02_UploadCsvFileOk_UtenzaConRuolo_GovIO_Sender() throws Exception {
		String fileName = "csv-test-UC102";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		//JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
	}
	
	// 3. Upload Fail file csv per utenza senza ruolo govio_sender
	@Test
	void UC_1_03_UploadCsvFileFail_UtenzaSenzaRuolo_GovIO_Sender() throws Exception {
		String fileName = "csv-test-UC103";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaOspite())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 4. Upload Fail file csv con parametro service_id non presente
	@Test
	void UC_1_04_UploadCsvFileFail_MissingServiceID() throws Exception {
		String fileName = "csv-test-UC104";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 5. Upload Fail file csv con parametro organization_id non presente
	@Test
	void UC_1_05_UploadCsvFileFail_MissingOrganizationID() throws Exception {
		String fileName = "csv-test-UC105";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		
		this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 6. Upload Fail file csv con service_id non presente nel db
	@Test
	void UC_1_06_UploadCsvFileFail_ServiceID_NonRegistrato() throws Exception {
		String fileName = "csv-test-UC106";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		int idNonPresente = 10000;
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, idNonPresente +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 7. Upload Fail file csv con organization_id non presente nel db
	@Test
	void UC_1_07_UploadCsvFileFail_OrganizationID_NonRegistrato() throws Exception {
		String fileName = "csv-test-UC107";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		int idNonPresente = 10000;
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, idNonPresente + "");
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		
		this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 8. Upload Fail file csv caricato gia' presente con stesso nome per service_id
	@Test
	void UC_1_08_UploadCsvFileOk_CsvDuplicato() throws Exception {
		String fileName = "csv-test-UC108";
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(fileName);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, Costanti.TEXT_CSV_CONTENT_TYPE, resourceAsStream);
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		this.mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, new URI("/files"))
				.file(mockMultipartFile)
				.params(params)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 9. Upload Fail file csv riferisce template non presente
		
}
