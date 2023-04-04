package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.json.Json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.entity.GovioPlaceholderEntity.Type;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione PlaceHolder")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_9_CreatePlaceHolderFailsTest {

	private static final String PLACEHOLDERS_BASE_PATH = "/v1/placeholders";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_9_01_CreatePlaceHolder_InvalidName() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", Costanti.STRING_256)
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_02_CreatePlaceHolder_InvalidExample() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", Costanti.STRING_256)
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_03_CreatePlaceHolder_InvalidPattern() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", Costanti.STRING_256)
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_04_CreatePlaceHolder_InvalidType() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Costanti.STRING_256)
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_05_CreatePlaceHolder_MissingName() throws Exception {
		String json = Json.createObjectBuilder()
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	
	@Test
	void UC_9_06_CreatePlaceHolder_MissingExample() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_07_CreatePlaceHolder_MissingType() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_08_CreatePlaceHolder_EmptyName() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_09_CreatePlaceHolder_EmptyExample() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_10_CreatePlaceHolder_EmptyType() throws Exception {
		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", "")
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_9_11_CreatePlaceHolder_InvalidURL() throws Exception {

		String json = Json.createObjectBuilder()
				.add("name", "NuovoPlaceHolder")
				.add("description", "PlaceHolder di test")
				.add("example", "PPPLLLAAACCCEEEHHHOOOLLDDDEEERRR")
				.add("type", Type.STRING.toString())
				.add("pattern", "[\\s\\S]*")
				.build()
				.toString();
		
	 this.mockMvc.perform(post("/v1//placeholders")
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
		
	}
}
