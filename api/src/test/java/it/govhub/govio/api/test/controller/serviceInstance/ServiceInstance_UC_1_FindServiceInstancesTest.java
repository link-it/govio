package it.govhub.govio.api.test.controller.serviceInstance;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

import javax.json.Json;
import javax.json.JsonArray;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.entity.GovioServiceInstanceEntity_;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura Service Instance")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class ServiceInstance_UC_1_FindServiceInstancesTest {

	private static final String SERVICE_INSTANCES_BASE_PATH = "/v1/service-instances";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Test
	void UC_4_01_FindAllOk() throws Exception {
		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH)
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
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		assertEquals("17886617e07d47e8b1ba314f2f1e3053", items.getJsonObject(0).getString("apiKey"));
		assertEquals("17886617e07d47e8b1ba314f2f1e3052", items.getJsonObject(1).getString("apiKey"));
		assertEquals("17886617e07d47e8b1ba314f2f1e3054", items.getJsonObject(2).getString("apiKey"));
	}

	@Test
	void UC_4_02_FindAllOk_Limit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "1");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject userList = reader.readObject();

		// Controlli sulla paginazione
		JsonObject page = userList.getJsonObject("page");
		assertEquals(0, page.getInt("offset"));
		assertEquals(1, page.getInt("limit"));
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(1, items.size());

		assertEquals("17886617e07d47e8b1ba314f2f1e3053", items.getJsonObject(0).getString("apiKey"));

	}

	@Test
	void UC_4_03_FindAllOk_InvalidLimit() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "XXX");

		this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	@Test
	void UC_4_04_FindAllOk_Offset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "1");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		//		JsonArray items = userList.getJsonArray("items");
		//		assertEquals(7, items.size());
	}

	@Test
	void UC_4_05_FindAllOk_InvalidOffset() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "XXX");

		this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.status", is(400)))
		.andExpect(jsonPath("$.title", is("Bad Request")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}

	@Test
	void UC_4_06_FindAllOk_Q() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_Q, "CIE");

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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

		assertEquals("17886617e07d47e8b1ba314f2f1e3054", items.getJsonObject(0).getString("apiKey"));
	}

	// servizio ordina per GovioServiceInstanceEntity_.ORGANIZATION +"."+OrganizationEntity_.LEGAL_NAME, GovioServiceInstanceEntity_.SERVICE + "." + ServiceEntity_.NAME)
	@Test
	void UC_4_09_FindAllOk_SortAsc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		assertEquals("17886617e07d47e8b1ba314f2f1e3054", items.getJsonObject(0).getString("apiKey"));
		assertEquals("17886617e07d47e8b1ba314f2f1e3052", items.getJsonObject(1).getString("apiKey"));
		assertEquals("17886617e07d47e8b1ba314f2f1e3053", items.getJsonObject(2).getString("apiKey"));
	}

	@Test
	void UC_4_10_FindAllOk_SortDesc() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);

		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
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
		assertEquals(3, page.getInt("total"));

		// Controlli sugli items
		JsonArray items = userList.getJsonArray("items");
		assertEquals(3, items.size());

		assertEquals("17886617e07d47e8b1ba314f2f1e3053", items.getJsonObject(0).getString("apiKey"));
		assertEquals("17886617e07d47e8b1ba314f2f1e3052", items.getJsonObject(1).getString("apiKey"));
		assertEquals("17886617e07d47e8b1ba314f2f1e3054", items.getJsonObject(2).getString("apiKey"));
	}
	//	
	//	@Test
	//	void UC_4_11_FindAllOk_OffsetLimit() throws Exception {
	//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	//		params.add(Costanti.USERS_QUERY_PARAM_OFFSET, "3");
	//		params.add(Costanti.USERS_QUERY_PARAM_LIMIT, "2");
	//		
	//		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
	//				.with(this.userAuthProfilesUtils.utenzaAdmin())
	//				.accept(MediaType.APPLICATION_JSON))
	//				.andExpect(status().isOk())
	//				.andReturn();
	//		
	//		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
	//		JsonObject userList = reader.readObject();
	//		
	//		// Controlli sulla paginazione
	//		JsonObject page = userList.getJsonObject("page");
	//		assertEquals(2, page.getInt("offset"));
	//		assertEquals(2, page.getInt("limit"));
	//		assertEquals(3, page.getInt("total"));
	//		
	//		// Controlli sugli items
	//		JsonArray items = userList.getJsonArray("items");
	//		assertEquals(2, items.size());
	//		
	//		assertEquals("Portale ZTL", items.getJsonObject(0).getString("service_name"));
	//		assertEquals("TARI", items.getJsonObject(1).getString("service_name"));
	//	}
	//	
	//	@Test
	//	void UC_4_12_FindAllOk_SortServiceNameDesc() throws Exception {
	//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT, "service_name");
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
	//		
	//		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
	//				.with(this.userAuthProfilesUtils.utenzaAdmin())
	//				.accept(MediaType.APPLICATION_JSON))
	//				.andExpect(status().isOk())
	//				.andReturn();
	//		
	//		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
	//		JsonObject userList = reader.readObject();
	//		
	//		// Controlli sulla paginazione
	//		JsonObject page = userList.getJsonObject("page");
	//		assertEquals(0, page.getInt("offset"));
	//		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
	//		assertEquals(3, page.getInt("total"));
	//		
	//		// Controlli sugli items
	//		JsonArray items = userList.getJsonArray("items");
	//		assertEquals(3, items.size());
	//
	//		assertEquals("Variazione Residenza", items.getJsonObject(0).getString("service_name"));
	//		assertEquals("TARI", items.getJsonObject(1).getString("service_name"));
	//		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(2).getString("service_name"));
	//		assertEquals("Servizio Generico", items.getJsonObject(3).getString("service_name"));
	//		assertEquals("Servizi Turistici", items.getJsonObject(4).getString("service_name"));
	//		assertEquals("SUAP-Integrazione", items.getJsonObject(5).getString("service_name"));
	//		assertEquals("Portale ZTL", items.getJsonObject(6).getString("service_name"));
	//		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(7).getString("service_name"));
	//		assertEquals("CIE", items.getJsonObject(8).getString("service_name"));
	//	}
	//	
	//	@Test
	//	void UC_4_13_FindAllOk_SortIdDesc() throws Exception {
	//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT, "id");
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
	//		
	//		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
	//				.with(this.userAuthProfilesUtils.utenzaAdmin())
	//				.accept(MediaType.APPLICATION_JSON))
	//				.andExpect(status().isOk())
	//				.andReturn();
	//		
	//		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
	//		JsonObject userList = reader.readObject();
	//		
	//		// Controlli sulla paginazione
	//		JsonObject page = userList.getJsonObject("page");
	//		assertEquals(0, page.getInt("offset"));
	//		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
	//		assertEquals(3, page.getInt("total"));
	//		
	//		// Controlli sugli items
	//		JsonArray items = userList.getJsonArray("items");
	//		assertEquals(3, items.size());
	//
	//		assertEquals("Servizi Turistici", items.getJsonObject(0).getString("service_name"));
	//		assertEquals("Variazione Residenza", items.getJsonObject(1).getString("service_name"));
	//		assertEquals("Portale ZTL", items.getJsonObject(2).getString("service_name"));
	//		assertEquals("TARI", items.getJsonObject(3).getString("service_name"));
	//		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(4).getString("service_name"));
	//		assertEquals("SUAP-Integrazione", items.getJsonObject(5).getString("service_name"));
	//		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(6).getString("service_name"));		
	//		assertEquals("Servizio Generico", items.getJsonObject(7).getString("service_name"));
	//		assertEquals("CIE", items.getJsonObject(8).getString("service_name"));
	//		
	//	}
	//	
	//	@Test
	//	void UC_4_14_FindAllOk_InvalidSortParam() throws Exception {
	//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT, "XXX");
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_DESC);
	//		
	//		this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
	//				.with(this.userAuthProfilesUtils.utenzaAdmin())
	//				.accept(MediaType.APPLICATION_JSON))
	//				.andExpect(status().isBadRequest())
	//				.andExpect(jsonPath("$.status", is(400)))
	//				.andExpect(jsonPath("$.title", is("Bad Request")))
	//				.andExpect(jsonPath("$.type").isString())
	//				.andExpect(jsonPath("$.detail").isString())
	//				.andReturn();
	//	}
	//	
	//	@Test
	//	void UC_4_15_FindAllOk_Sort_Unsorted() throws Exception {
	//		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT, "unsorted");
	//		params.add(Costanti.USERS_QUERY_PARAM_SORT_DIRECTION, Costanti.QUERY_PARAM_SORT_DIRECTION_ASC);
	//		
	//		MvcResult result = this.mockMvc.perform(get(SERVICE_INSTANCES_BASE_PATH).params(params )
	//				.with(this.userAuthProfilesUtils.utenzaAdmin())
	//				.accept(MediaType.APPLICATION_JSON))
	//				.andExpect(status().isOk())
	//				.andReturn();
	//		
	//		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
	//		JsonObject userList = reader.readObject();
	//		
	//		// Controlli sulla paginazione
	//		JsonObject page = userList.getJsonObject("page");
	//		assertEquals(0, page.getInt("offset"));
	//		assertEquals(Costanti.USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE, page.getInt("limit"));
	//		assertEquals(3, page.getInt("total"));
	//			
	//		// Controlli sugli items
	//		JsonArray items = userList.getJsonArray("items");
	//		assertEquals(3, items.size());
	//
	//		assertEquals("CIE", items.getJsonObject(0).getString("service_name"));
	//		assertEquals("Servizio Generico", items.getJsonObject(1).getString("service_name"));
	//		assertEquals("Servizio senza autorizzazioni", items.getJsonObject(2).getString("service_name"));
	//		assertEquals("SUAP-Integrazione", items.getJsonObject(3).getString("service_name"));
	//		assertEquals("IMU-ImpostaMunicipaleUnica", items.getJsonObject(4).getString("service_name"));
	//		assertEquals("TARI", items.getJsonObject(5).getString("service_name"));
	//		assertEquals("Portale ZTL", items.getJsonObject(6).getString("service_name"));
	//		assertEquals("Variazione Residenza", items.getJsonObject(7).getString("service_name"));
	//		assertEquals("Servizi Turistici", items.getJsonObject(8).getString("service_name"));
	//	}
}
