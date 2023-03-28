package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import it.govhub.govio.api.entity.GovioPlaceholderEntity.Type;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione PlaceHolder")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_8_CreatePlaceHolderTest {

	private static final String PLACEHOLDERS_BASE_PATH = "/v1/placeholders";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Autowired
	PlaceholderRepository placeholderRepository;
	
	@Test
	void UC_8_01_CreatePlaceHolderOk() throws Exception {

		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		MvcResult result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name", is("NuovoPlaceHolder")))
				.andExpect(jsonPath("$.description", is("PlaceHolder di test")))
				.andExpect(jsonPath("$.example", is("PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")))
				.andExpect(jsonPath("$.type", is(Type.STRING.toString())))
				.andExpect(jsonPath("$.pattern", is("[\\s\\S]*")))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		int id = si.getInt("id");
		
		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById((long) id).get();
		
		assertEquals(id, govioPlaceholderEntity.getId());
		assertEquals(si.getString("name"), govioPlaceholderEntity.getName());
		assertEquals(si.getString("description"), govioPlaceholderEntity.getDescription());
		assertEquals(si.getString("example"), govioPlaceholderEntity.getExample());
		assertEquals(si.getString("type"), govioPlaceholderEntity.getType().toString());
		assertEquals(si.getString("pattern"), govioPlaceholderEntity.getPattern());
	}

	@Test
	void UC_8_02_CreatePlaceHolderOk_PlaceHolderDuplicato() throws Exception {

		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		MvcResult result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name", is("NuovoPlaceHolder")))
				.andExpect(jsonPath("$.description", is("PlaceHolder di test")))
				.andExpect(jsonPath("$.example", is("PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")))
				.andExpect(jsonPath("$.type", is(Type.STRING.toString())))
				.andExpect(jsonPath("$.pattern", is("[\\s\\S]*")))
				.andReturn();
		
		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		int id = si.getInt("id");
		
		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById((long) id).get();
		
		assertEquals(id, govioPlaceholderEntity.getId());
		assertEquals(si.getString("name"), govioPlaceholderEntity.getName());
		assertEquals(si.getString("description"), govioPlaceholderEntity.getDescription());
		assertEquals(si.getString("example"), govioPlaceholderEntity.getExample());
		assertEquals(si.getString("type"), govioPlaceholderEntity.getType().toString());
		assertEquals(si.getString("pattern"), govioPlaceholderEntity.getPattern());
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.name", is("NuovoPlaceHolder")))
				.andExpect(jsonPath("$.description", is("PlaceHolder di test")))
				.andExpect(jsonPath("$.example", is("PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")))
				.andExpect(jsonPath("$.type", is(Type.STRING.toString())))
				.andExpect(jsonPath("$.pattern", is("[\\s\\S]*")))
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		si = reader.readObject();
		id = si.getInt("id");
		
		govioPlaceholderEntity = this.placeholderRepository.findById((long) id).get();
		
		assertEquals(id, govioPlaceholderEntity.getId());
		assertEquals(si.getString("name"), govioPlaceholderEntity.getName());
		assertEquals(si.getString("description"), govioPlaceholderEntity.getDescription());
		assertEquals(si.getString("example"), govioPlaceholderEntity.getExample());
		assertEquals(si.getString("type"), govioPlaceholderEntity.getType().toString());
		assertEquals(si.getString("pattern"), govioPlaceholderEntity.getPattern());
	}
}