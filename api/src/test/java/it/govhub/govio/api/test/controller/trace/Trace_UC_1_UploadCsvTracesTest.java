package it.govhub.govio.api.test.controller.trace;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioFileEntity.Status;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.MultipartUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;


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
	private ReadOrganizationRepository organizationRepository;
	
	@Autowired
	private ReadServiceRepository serviceRepository;
	
	@Autowired
	private GovioFileRepository govioFileRepository;
	
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
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		MvcResult result = this.mockMvc.perform(
				multipart("/files")
            	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.filename", is(fileName)))
				.andExpect(jsonPath("$.organization.tax_code", is(ente.getTaxCode())))
				.andExpect(jsonPath("$.service.service_name", is(servizio.getName())))
				.andExpect(jsonPath("$.status", is(Status.CREATED.toString())))
				
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		GovioFileEntity govioFileEntity = this.govioFileRepository.findById((long) id).get();
		
		assertEquals(id, govioFileEntity.getId());
		assertEquals(fileName, govioFileEntity.getName());
		assertEquals("amministratore", govioFileEntity.getGovauthUser().getPrincipal());
		assertEquals(ente.getTaxCode(), govioFileEntity.getServiceInstance().getOrganization().getTaxCode());
		assertEquals(servizio.getName(), govioFileEntity.getServiceInstance().getService().getName());
		assertEquals(Status.CREATED, govioFileEntity.getStatus());
	}
	
	// 2. Upload OK file csv per utenza con ruolo govio_sender
	@Test
	void UC_1_02_UploadCsvFileOk_UtenzaConRuolo_GovIO_Sender() throws Exception {
		String fileName = "csv-test-UC102";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		MvcResult result = this.mockMvc.perform(
				multipart("/files")
            	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		GovioFileEntity govioFileEntity = this.govioFileRepository.findById((long) id).get();
		
		assertEquals(id, govioFileEntity.getId());
		assertEquals(fileName, govioFileEntity.getName());
		assertEquals("govio_sender", govioFileEntity.getGovauthUser().getPrincipal());
		assertEquals(ente.getTaxCode(), govioFileEntity.getServiceInstance().getOrganization().getTaxCode());
		assertEquals(servizio.getName(), govioFileEntity.getServiceInstance().getService().getName());
		assertEquals(Status.CREATED, govioFileEntity.getStatus());
	}
	
	// 3. Upload Fail file csv per utenza senza ruolo govio_sender
	// TODO riabilitare
//	@Test
	void UC_1_03_UploadCsvFileFail_UtenzaSenzaRuolo_GovIO_Sender() throws Exception {
		String fileName = "csv-test-UC103";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status", is(401)))
				.andExpect(jsonPath("$.title", is("Unauthorized")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 4. Upload Fail file csv con parametro service_id non presente
	// TODO riabilitare
//	@Test
	void UC_1_04_UploadCsvFileFail_MissingServiceID() throws Exception {
		String fileName = "csv-test-UC104";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	// 5. Upload Fail file csv con parametro organization_id non presente
	// TODO riabilitare
//	@Test
	void UC_1_05_UploadCsvFileFail_MissingOrganizationID() throws Exception {
		String fileName = "csv-test-UC105";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
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
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		int idNonPresente = 10000;
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, idNonPresente +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
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
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		int idNonPresente = 10000;
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, idNonPresente + "");
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
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
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		MvcResult result = this.mockMvc.perform(
				multipart("/files")
            	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");
		
		GovioFileEntity govioFileEntity = this.govioFileRepository.findById((long) id).get();
		
		assertEquals(id, govioFileEntity.getId());
		assertEquals(fileName, govioFileEntity.getName());
		assertEquals("amministratore", govioFileEntity.getGovauthUser().getPrincipal());
		assertEquals(ente.getTaxCode(), govioFileEntity.getServiceInstance().getOrganization().getTaxCode());
		assertEquals(servizio.getName(), govioFileEntity.getServiceInstance().getService().getName());
		assertEquals(Status.CREATED, govioFileEntity.getStatus());
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 9. Upload Fail file csv riferisce serviceinstance non presente
	@Test
	void UC_1_09_UploadCsvFileFail_ServiceInstance_NonRegistrato() throws Exception {
		String fileName = "csv-test-UC109";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();
		
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_TARI);
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_ID, servizio.getId() +"");
		params.add(Costanti.PARAMETRO_ORGANIZATION_ID, ente.getId() + "");
		
		this.mockMvc.perform(
				multipart("/files")
                	.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
					.params(params)
					.contentType("multipart/form-data; boundary=" + boundary)
					.characterEncoding("UTF-8")
					.with(this.userAuthProfilesUtils.utenzaAdmin())
					.with(csrf())
					.accept(MediaType.APPLICATION_JSON)
					)
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
		
}
