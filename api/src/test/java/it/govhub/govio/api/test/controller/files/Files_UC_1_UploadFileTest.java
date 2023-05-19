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
package it.govhub.govio.api.test.controller.files;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
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
import org.springframework.data.jpa.domain.Specification;
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
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceFilters;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
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

class Files_UC_1_UploadFileTest {

	private static final String FILES_BASE_PATH = "/v1/files";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	private ReadOrganizationRepository organizationRepository;

	@Autowired
	private ReadServiceRepository serviceRepository;

	@Autowired
	private FileRepository govioFileRepository;

	@Autowired
	private ServiceInstanceRepository serviceInstanceRepository;

	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}

	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}

	private GovioServiceInstanceEntity leggiServiceInstanceDB(Long idOrganization, Long idService) {

		Specification<GovioServiceInstanceEntity> spec = ServiceInstanceFilters.empty();

		spec = spec.and(ServiceInstanceFilters.byOrganizationIds(Arrays.asList(idOrganization)));
		spec = spec.and(ServiceInstanceFilters.byServiceIds(Arrays.asList(idService)));

		List<GovioServiceInstanceEntity> findAll = this.serviceInstanceRepository.findAll(spec);

		return findAll.size() > 0 ? findAll.get(0) : null;
	}

	// 1. Upload OK file csv per utenza admin 
	@Test
	void UC_1_01_UploadCsvFileOk_Utenza_Admin() throws Exception {
		String fileName = "csv-test-UC101";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		MvcResult result = this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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
				.andExpect(jsonPath("$.service_instance_id", is((int) serviceInstanceEntity.getId().longValue())))
				.andExpect(jsonPath("$.status", is(Status.CREATED.toString())))
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		GovioFileEntity govioFileEntity = this.govioFileRepository.findById((long) id).get();

		assertEquals(id, govioFileEntity.getId());
		assertEquals(fileName, govioFileEntity.getName());
		assertEquals("amministratore", govioFileEntity.getGovauthUser().getPrincipal());
		assertEquals(serviceInstanceEntity.getId(), govioFileEntity.getServiceInstance().getId());
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

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		MvcResult result = this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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
		assertEquals("user_govio_sender", govioFileEntity.getGovauthUser().getPrincipal());
		assertEquals(ente.getTaxCode(), govioFileEntity.getServiceInstance().getOrganization().getTaxCode());
		assertEquals(servizio.getName(), govioFileEntity.getServiceInstance().getService().getName());
		assertEquals(Status.CREATED, govioFileEntity.getStatus());
	}

	// 3. Upload Fail file csv per utenza senza ruolo govio_sender
	@Test
	void UC_1_03_UploadCsvFileFail_UtenzaSenzaRuolo_GovIO_Sender() throws Exception {
		String fileName = "csv-test-UC103";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
				.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaOspite())
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

	// 4. Upload Fail file csv con parametro serviceInstance_id non presente
	@Test
	void UC_1_04_UploadCsvFileFail_MissingServiceInstanceID() throws Exception {
		String fileName = "csv-test-UC104";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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


	// 6. Upload Fail file csv con service_instance_id non presente nel db
	@Test
	void UC_1_06_UploadCsvFileFail_ServiceID_NonRegistrato() throws Exception {
		String fileName = "csv-test-UC106";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		int idNonPresente = 10000;

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, idNonPresente +"");

		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		MvcResult result = this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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
		assertEquals(serviceInstanceEntity.getId(), govioFileEntity.getServiceInstance().getId());
		assertEquals(ente.getTaxCode(), govioFileEntity.getServiceInstance().getOrganization().getTaxCode());
		assertEquals(servizio.getName(), govioFileEntity.getServiceInstance().getService().getName());
		assertEquals(Status.CREATED, govioFileEntity.getStatus());

		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
				.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
		.andExpect(status().isConflict())
		.andExpect(jsonPath("$.status", is(409)))
		.andExpect(jsonPath("$.title", is("Conflict")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	// 10. Upload Fail file filename vuoto.
	@Test
	void UC_1_10_UploadCsvFileFail_MissingFilename() throws Exception {
		String fileName = "";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		//		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, 2+"");


		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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

	// 11. Upload OK Invio di un file per una utenza che ha autorizzazione su un solo service e una organization
	@Test
	void UC_1_11_UploadCsvFileOk_AuthorizedSI() throws Exception {
		String fileName = "csv-test-UC111";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_CIE_ORG);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_CIE);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		MvcResult result = this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
				.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAutorizzataSI())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.filename", is(fileName)))
				.andExpect(jsonPath("$.service_instance_id", is((int) serviceInstanceEntity.getId().longValue())))
				.andExpect(jsonPath("$.status", is(Status.CREATED.toString())))
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		int id = reader.readObject().getInt("id");

		GovioFileEntity govioFileEntity = this.govioFileRepository.findById((long) id).get();

		assertEquals(id, govioFileEntity.getId());
		assertEquals(fileName, govioFileEntity.getName());
		assertEquals("user_govio_sender_si", govioFileEntity.getGovauthUser().getPrincipal());
		assertEquals(serviceInstanceEntity.getId(), govioFileEntity.getServiceInstance().getId());
		assertEquals(ente.getTaxCode(), govioFileEntity.getServiceInstance().getOrganization().getTaxCode());
		assertEquals(servizio.getName(), govioFileEntity.getServiceInstance().getService().getName());
		assertEquals(Status.CREATED, govioFileEntity.getStatus());

	}

	// 11. Upload Fail Invio di un file per un service istance associato ad una organization e service non autorizzati per l'utenza chiamante
	@Test
	void UC_1_12_UploadCsvFileOk_OrganizationNotAuthorized() throws Exception {
		String fileName = "csv-test-UC111";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
				.content(MultipartUtils.createFileContent(content, boundary,  Costanti.TEXT_CSV_CONTENT_TYPE, fileName))
				.params(params)
				.contentType("multipart/form-data; boundary=" + boundary)
				.characterEncoding("UTF-8")
				.with(this.userAuthProfilesUtils.utenzaAutorizzataSI())
				.with(csrf())
				.accept(MediaType.APPLICATION_JSON)
				)
		.andExpect(status().isUnauthorized())
		.andReturn();


	}

	// Upload Fail Service Instance disabilitato
	@Test
	void UC_1_12_UploadCsvFileFail_ServiceInstanceDisabled() throws Exception {
		String fileName = "csv-test-UC112";
		byte[] content = FileUtils.readFileToByteArray(new ClassPathResource("csv-test").getFile());
		String boundary = MultipartUtils.generateBoundary();

		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_2);
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_IMU);

		GovioServiceInstanceEntity serviceInstanceEntity = leggiServiceInstanceDB(ente.getId(), servizio.getId());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.PARAMETRO_SERVICE_INSTANCE_ID, serviceInstanceEntity.getId() +"");

		this.mockMvc.perform(
				multipart(FILES_BASE_PATH)
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
