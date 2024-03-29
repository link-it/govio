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
package it.govhub.govio.api.test.controller.files;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
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

import it.govhub.govio.api.Application;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.GovioFileUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.services.GovhubUserDetailService;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura csv tracciati")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Files_UC_4_GetFileContentTest {

	private static final String FILES_BASE_PATH = "/v1/files";
	private static final String FILES_BASE_PATH_DETAIL_ID = FILES_BASE_PATH + "/{id}/content";

	@Value("${govio.filerepository.path}")
	Path fileRepositoryPath;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	private FileRepository govioFilesRepository;
	
	@Autowired
	private ServiceInstanceRepository govioServiceInstancesRepository;
	
	@Autowired
	private GovhubUserDetailService userDetailService;
	
	@BeforeEach
	void setUp() throws Exception{
		govioFilesRepository.deleteAll();
		
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		
		UserEntity user = ((GovhubPrincipal) this.userDetailService.loadUserByUsername("user_govio_sender")).getUser();
		
		List<GovioFileEntity> files = new ArrayList<>();
		files.add(govioFilesRepository.save(GovioFileUtils.buildFile(this.fileRepositoryPath, serviceInstanceEntity.get(), "01", user)));
		files.add(govioFilesRepository.save(GovioFileUtils.buildFile(this.fileRepositoryPath, serviceInstanceEntity.get(), "02", user)));
	}
	
	// 1. getFileOK 
	@Test
	void UC_4_01_GetFileOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
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
		
		
		JsonObject item1 = items.getJsonObject(0);
		int idFile = item1.getInt("id");
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andReturn();
		
		String fileReceived = result.getResponse().getContentAsString(); 
		
		GovioFileEntity govioFileEntity = this.govioFilesRepository.findById((long) idFile).get();
		
		Path path = govioFileEntity.getLocation();
		
		FileInputStream stream;
		try {
			stream = new FileInputStream(path.toFile());
		} catch (FileNotFoundException e) {
			throw new InternalException(e);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(stream, baos);
		
		assertEquals(fileReceived, baos.toString());
	}
	
	// 2. getNotFound
	@Test
	void UC_4_02_GetFile_NotFound() throws Exception {
		int idFile = 10000;
		
		this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/problem+json"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 3. getInvalidID
	@Test
	void UC_4_03_GetFile_InvalidID() throws Exception {
		String idFile = "XXX";
		
		this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/problem+json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 4. getFile Fail file eliminato dal server 
	@Test
	void UC_4_04_GetFile_FileNotFoundOnFileSystem() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
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
		
		
		JsonObject item1 = items.getJsonObject(0);
		int idFile = item1.getInt("id");
		
		GovioFileEntity govioFileEntity = this.govioFilesRepository.findById((long) idFile).get();
		
		
		File contenutoFileDaEliminare = govioFileEntity.getLocation().toFile();
		if(contenutoFileDaEliminare.exists()) {
			contenutoFileDaEliminare.delete();
		}

		this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM_VALUE, "application/problem+json"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status", is(500)))
				.andExpect(jsonPath("$.title", is("Internal Server Error")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
}
