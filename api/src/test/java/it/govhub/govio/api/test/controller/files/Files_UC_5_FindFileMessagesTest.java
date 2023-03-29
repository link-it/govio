package it.govhub.govio.api.test.controller.files;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.beans.FileMessageStatusEnum;
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.FileRepository;
import it.govhub.govio.api.repository.ServiceInstanceRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.GovioFileUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.services.GovhubUserDetailService;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura messaggi estratti dal csv")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Files_UC_5_FindFileMessagesTest {

	private static final String FILES_BASE_PATH = "/v1/files";
	private static final String FILES_BASE_PATH_DETAIL_ID = FILES_BASE_PATH + "/{id}/file-messages";

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
	
	// 1. getFileMessagesOK 
	@Test
	void UC_3_01_GetFileMessagesOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	// 2. getFileMessages FileNotFound
	@Test
	void UC_3_02_GetFileMessages_NotFound() throws Exception {
		int idFile = 10000;
		
		this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 3. getFileMessages InvalidID
	@Test
	void UC_3_03_GetFileMessages_InvalidID() throws Exception {
		String idFile = "XXX";
		
		this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	// 4. getFileMessages Filtro sullo stato del messaggio 'acquired'
	@Test
	void UC_3_04_GetFileMessagesOk_FileMessageStatusAcquired() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_FILE_MESSAGE_STATUS, FileMessageStatusEnum.ACQUIRED.toString());
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	
	// 5. getFileMessages Filtro sullo stato del messaggio 'error'
	@Test
	void UC_3_05_GetFileMessagesOk_FileMessageStatusError() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_FILE_MESSAGE_STATUS, FileMessageStatusEnum.ERROR.toString());
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	// 6. getFileMessages Filtro sullo stato del messaggio 'any'
	@Test
	void UC_3_06_GetFileMessagesOk_FileMessageStatusAny() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_FILE_MESSAGE_STATUS, FileMessageStatusEnum.ANY.toString());
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	// 7. getFileMessages Filtro sulla linea del file da restiuire l >  0
	@Test
	void UC_3_07_GetFileMessagesOk_LinNumberFromGreaterThanZero() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_LINE_NUMBER_FROM, "1");
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	// 8. getFileMessages Filtro sulla linea del file da restiuire l < 0
	@Test
	void UC_3_08_GetFileMessagesOk_LinNumberFromLessThanZero() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_LINE_NUMBER_FROM, "-1");
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail", is("readFileMessages.lineNumberFrom: must be greater than or equal to 0")))
				.andReturn();
	}
	
	// 9. getFileMessages Filtro sulla linea del file da restiuire l = 0
	@Test
	void UC_3_09_GetFileMessagesOk_LinNumberFromEqualsZero() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_LINE_NUMBER_FROM, "0");
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
	// 10. getFileMessages Filtro sulla linea del file da restiuire l > size
	@Test
	void UC_3_10_GetFileMessagesOk_LinNumberFromGreaterThanSize() throws Exception {
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
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
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_LINE_NUMBER_FROM, "1");
		
		result = this.mockMvc.perform(get(FILES_BASE_PATH_DETAIL_ID,idFile).params(params)
				.with(this.userAuthProfilesUtils.utenzaGovIOSender())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		userList = reader.readObject();
		
		// Controlli sulla paginazione
		page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
		assertEquals(0, page.getInt("total"));
		
		// Controlli sugli items
		items = userList.getJsonArray("items");
		assertEquals(0, items.size());
	}
	
}
