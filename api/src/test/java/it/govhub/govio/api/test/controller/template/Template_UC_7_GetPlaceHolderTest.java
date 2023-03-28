package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;

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
import it.govhub.govio.api.entity.GovioPlaceholderEntity;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di lettura PlaceHolder")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_7_GetPlaceHolderTest {

	private static final String PLACEHOLDERS_BASE_PATH = "/v1/placeholders";
	private static final String PLACEHOLDERS_BASE_PATH_DETAIL_ID = PLACEHOLDERS_BASE_PATH + "/{id}";

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	PlaceholderRepository placeholderRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_7_01_GetPlaceHolderOk() throws Exception {
		GovioPlaceholderEntity placeholderEntity = this.placeholderRepository.findById(1l).get();
		
		long idService1 = placeholderEntity.getId();
		
		MvcResult result = this.mockMvc.perform(get(PLACEHOLDERS_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject item = reader.readObject();
		
		assertEquals(placeholderEntity.getName(), item.getString("name"));
		assertEquals(placeholderEntity.getType().toString(), item.getString("type"));
		assertEquals(placeholderEntity.getExample(), item.getString("example"));
		assertEquals(placeholderEntity.getDescription(), item.getString("description"));
		assertNull(placeholderEntity.getPattern());
		
	}
	
	@Test
	void UC_7_02_GetTemplate_NotFound() throws Exception {
		int idService1 = 10000;
		
		this.mockMvc.perform(get(PLACEHOLDERS_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_7_03_GetTemplate_InvalidId() throws Exception {
		String idService1 = "XXX";
		
		this.mockMvc.perform(get(PLACEHOLDERS_BASE_PATH_DETAIL_ID,idService1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
