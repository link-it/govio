package it.govhub.govio.api.test.controller.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
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
import it.govhub.govio.api.entity.GovioFileEntity;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity;
import it.govhub.govio.api.repository.GovioFileRepository;
import it.govhub.govio.api.repository.GovioServiceInstanceRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.GovioFileUtils;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.security.beans.GovhubPrincipal;
import it.govhub.security.services.GovhubUserDetailService;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura csv tracciati")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class Files_UC_2_FindFilesTest {

	private static final String FILES_BASE_PATH = "/v1/files";

	@Value("${govio.filerepository.path}")
	Path fileRepositoryPath;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	private GovioFileRepository govioFilesRepository;
	
	@Autowired
	private GovioServiceInstanceRepository govioServiceInstancesRepository;
	
	@Autowired
	private GovhubUserDetailService userDetailService;
	
	private DateTimeFormatter dt = DateTimeFormatter.ISO_DATE_TIME;
	
	@BeforeEach
	void setUp() throws Exception{
		govioFilesRepository.deleteAll();
		
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		
		UserEntity user = ((GovhubPrincipal) this.userDetailService.loadUserByUsername("govio_sender")).getUser();
		
		List<GovioFileEntity> files = new ArrayList<>();
		files.add(govioFilesRepository.save(GovioFileUtils.buildFile(this.fileRepositoryPath, serviceInstanceEntity.get(), "01", user)));
		files.add(govioFilesRepository.save(GovioFileUtils.buildFile(this.fileRepositoryPath, serviceInstanceEntity.get(), "02", user)));
	}
	
	// 1. findAllOK senza filtri 
	@Test
	void UC_2_01_FindAllOk() throws Exception {
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 2. findAllOk filtro data creazione da
	@Test
	void UC_2_02_FindAllOk_CreationDateFrom() throws Exception {
		OffsetDateTime now = OffsetDateTime.now().minusMinutes(10); 
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_CREATION_DATE_FROM, dt.format(now));
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 3. findAllOk filtro data creazione a
	@Test
	void UC_2_03_FindAllOk_CreationDateTo() throws Exception {
		OffsetDateTime now = OffsetDateTime.now().plusMinutes(1); 
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_CREATION_DATE_TO, dt.format(now));
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 4. findAllOk filtro data creazione intervallo
	@Test
	void UC_2_04_FindAllOk_CreationDateInterval() throws Exception {
		OffsetDateTime now = OffsetDateTime.now(); 
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_CREATION_DATE_FROM, dt.format(now.minusMinutes(10)));
		params.add(Costanti.FILES_QUERY_PARAM_CREATION_DATE_TO, dt.format(now.plusMinutes(1)));
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 5. findAllOk filtro data creazione da maggiore di ora = zero risultati
	@Test
	void UC_2_05_FindAllOk_CreationDateFromGreaterThanNow() throws Exception {
		OffsetDateTime now = OffsetDateTime.now(); 
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_CREATION_DATE_FROM, dt.format(now.plusHours(1)));
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
	
	// 6. findAllOk filtro data creazione a minore di ora = zero risultati
	@Test
	void UC_2_06_FindAllOk_CreationDateToLessThanNow() throws Exception {
		OffsetDateTime now = OffsetDateTime.now(); 
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_CREATION_DATE_TO, dt.format(now.minusHours(1)));
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
	
	// 7. findAllOk filtro like sul nome
	@Test
	void UC_2_07_FindAllOk_Name() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "02");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		
		
		JsonObject item1 = items.getJsonObject(0);
		
		assertEquals("02.csv", item1.getString("filename"));
	}
	
	// 8. findAllOk filtro sull'utente che ha caricato il tracciato govio_sender
	@Test
	void UC_2_08_FindAllOk_UserID() throws Exception {
		UserEntity user = ((GovhubPrincipal) this.userDetailService.loadUserByUsername("govio_sender")).getUser();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_USER_ID, user.getId()+"");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 9. findAllOk filtro sull'utente che ha caricato il tracciato altro utente senza csv = zero risultati
	@Test
	void UC_2_09_FindAllOk_UserIDNoFiles() throws Exception {
		UserEntity user = ((GovhubPrincipal) this.userDetailService.loadUserByUsername("ospite")).getUser();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_USER_ID, user.getId()+"");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
	
	// 10. findAllOk filtro service id presente
	@Test
	void UC_2_10_FindAllOk_ServiceID() throws Exception {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_SERVICE_ID, serviceInstanceEntity.get().getService().getId()+"");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 11. findAllOk filtro service id senza csv = zero risultati
	@Test
	void UC_2_11_FindAllOk_ServiceIDNoFiles() throws Exception {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(2L);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_SERVICE_ID, serviceInstanceEntity.get().getService().getId()+"");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
	
	// 12. findAllOk filtro organization id presente
	@Test
	void UC_2_12_FindAllOk_OrganizationID() throws Exception {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_ORGANIZATION_ID, serviceInstanceEntity.get().getOrganization().getId()+"");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
	// 13. findAllOk filtro organization id senza csv = zero risultati
	@Test
	void UC_2_13_FindAllOk_OrganizationIDNoFiles() throws Exception {
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(2L);
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.FILES_QUERY_PARAM_ORGANIZATION_ID, serviceInstanceEntity.get().getOrganization().getId()+"");
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params)
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
	
	// 14. findAllOk filtro ordinamento per creation_date asc
	@Test
	void UC_2_14_FindAllOk_SortCreationDateAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params )
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("01.csv", item1.getString("filename"));
		assertEquals("02.csv", item2.getString("filename"));
	}
	
	// 15. findAllOk filtro ordinamento per creation_date desc
	@Test
	void UC_2_15_FindAllOk_SortCreationDateDesc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
		
		MvcResult result = this.mockMvc.perform(get(FILES_BASE_PATH).params(params )
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
		JsonObject item2 = items.getJsonObject(1);
		
		assertEquals("02.csv", item1.getString("filename"));
		assertEquals("01.csv", item2.getString("filename"));
	}
	
}
