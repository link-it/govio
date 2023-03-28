package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

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
import it.govhub.govio.api.entity.GovioTemplateEntity;
import it.govhub.govio.api.entity.GovioTemplatePlaceholderEntity;
import it.govhub.govio.api.repository.PlaceholderRepository;
import it.govhub.govio.api.repository.TemplatePlaceholderRepository;
import it.govhub.govio.api.repository.TemplateRepository;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di creazione PlaceHolder di un Template")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_12_CreateTemplatePlaceHolderTest {

	private static final String TEMPLATES_BASE_PATH = "/v1/templates";
	private static final String TEMPLATES_BASE_PATH_DETAIL_ID = TEMPLATES_BASE_PATH + "/{id}";
	private static final String PLACEHOLDERS_BASE_PATH = TEMPLATES_BASE_PATH_DETAIL_ID + "/placeholders";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;

	@Autowired
	TemplateRepository templateRepository;

	@Autowired
	PlaceholderRepository placeholderRepository;

	@Autowired
	TemplatePlaceholderRepository templatePlaceholderRepository;

	@Test
	void UC_12_01_CreatePlaceHolderOk() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();

		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById(2l).get();
		long idPlaceHolder = govioPlaceholderEntity.getId();

		String json = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 2)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.placeholder_id").isNumber())
				.andExpect(jsonPath("$.mandatory", is(true)))
				.andExpect(jsonPath("$.position", is(2)))
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		long id = si.getInt("placeholder_id");

		List<GovioTemplatePlaceholderEntity> findAll = this.templatePlaceholderRepository.findAll();

		GovioTemplatePlaceholderEntity govioTemplatePlaceholderEntity = findAll.stream().filter(p -> p.getId().getGovioPlaceholder().longValue() == id).collect(Collectors.toList()).get(0);

		assertEquals(id, govioTemplatePlaceholderEntity.getId().getGovioPlaceholder());
		assertEquals(si.getBoolean("mandatory"), govioTemplatePlaceholderEntity.isMandatory());
		assertEquals(si.getInt("position"), govioTemplatePlaceholderEntity.getPosition());
	}

	@Test
	void UC_12_02_CreatePlaceHolderOk_PlaceHolderGiaAssegnatoAlTemplate() throws Exception {

		GovioTemplateEntity templateEntity = this.templateRepository.findById(2l).get();
		long idTemplate1 = templateEntity.getId();

		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById(3l).get();
		long idPlaceHolder = govioPlaceholderEntity.getId();

		String json = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 3)
				.build()
				.toString();

		MvcResult result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.placeholder_id").isNumber())
				.andExpect(jsonPath("$.mandatory", is(true)))
				.andExpect(jsonPath("$.position", is(3)))
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		long id = si.getInt("placeholder_id");

		List<GovioTemplatePlaceholderEntity> findAll = this.templatePlaceholderRepository.findAll();

		GovioTemplatePlaceholderEntity govioTemplatePlaceholderEntity = findAll.stream().filter(p -> p.getId().getGovioPlaceholder().longValue() == id).collect(Collectors.toList()).get(0);

		assertEquals(id, govioTemplatePlaceholderEntity.getId().getGovioPlaceholder());
		assertEquals(si.getBoolean("mandatory"), govioTemplatePlaceholderEntity.isMandatory());
		assertEquals(si.getInt("position"), govioTemplatePlaceholderEntity.getPosition());

		// Assegno lo stesso placeholder al template nella stessa posizione
		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isConflict())
		.andExpect(jsonPath("$.status", is(409)))
		.andExpect(jsonPath("$.title", is("Conflict")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		// Assegno lo stesso placeholder al template in posizione diversa
		json = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 4)
				.build()
				.toString();

		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isConflict())
		.andExpect(jsonPath("$.status", is(409)))
		.andExpect(jsonPath("$.title", is("Conflict")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();

		// Assegno un altroplaceholder al template in posizione occupata
		json = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 3)
				.build()
				.toString();

		govioPlaceholderEntity = this.placeholderRepository.findById(4l).get();
		idPlaceHolder = govioPlaceholderEntity.getId();

		this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isConflict())
		.andExpect(jsonPath("$.status", is(409)))
		.andExpect(jsonPath("$.title", is("Conflict")))
		.andExpect(jsonPath("$.type").isString())
		.andExpect(jsonPath("$.detail").isString())
		.andReturn();
	}
}
