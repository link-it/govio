package it.govhub.govio.api.test.controller.template;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

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
@DisplayName("Test di modifica Place Holder")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Template_UC_13_PatchTemplatePlaceHolderTest {

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

//	@Test
	void UC_13_01_PatchPlaceHolderOk() throws Exception {
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


		govioPlaceholderEntity = this.placeholderRepository.findById(3l).get();
		idPlaceHolder = govioPlaceholderEntity.getId();

		json = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 3)
				.build()
				.toString();

		result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.placeholder_id").isNumber())
				.andExpect(jsonPath("$.mandatory", is(false)))
				.andExpect(jsonPath("$.position", is(3)))
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		si = reader.readObject();
		long id2 = si.getInt("placeholder_id");

		findAll = this.templatePlaceholderRepository.findAll();

		govioTemplatePlaceholderEntity = findAll.stream().filter(p -> p.getId().getGovioPlaceholder().longValue() == id2).collect(Collectors.toList()).get(0);
		
		// place holder presenti ora: p1 in posizione 1 mandatory=true,  p2 in posizione 2 mandatory=true,  p3 in posizione 3 mandatory=false
		// scambio i placeholder p1 in pos3 mandatory=false, p2 in pos1 mandatory=true e p3 in pos2 mandatory = true
		
		JsonObject obj1 = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 3)
				.add("placeholder_id", 1)
				.build();
		
		JsonObject obj2 = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 1)
				.add("placeholder_id", 2)
				.build();
		
		JsonObject obj3 = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 2)
				.add("placeholder_id", 3)
				.build();
		
		JsonArray array = Json.createArrayBuilder()
				.add(obj1)
				.add(obj2)
				.add(obj3)
				.build();
		json = Json.createObjectBuilder()
				.add("items", array)
				.build()
				.toString();
		
		result = this.mockMvc.perform(put(PLACEHOLDERS_BASE_PATH, idTemplate1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		si = reader.readObject();
//		long id3 = si.getInt("placeholder_id");
		
	}

	@Test
	void UC_13_02_PatchPlaceHolderFail_DuplicatePosition() throws Exception {
		GovioTemplateEntity templateEntity = this.templateRepository.findById(1l).get();
		long idTemplate1 = templateEntity.getId();

		GovioPlaceholderEntity govioPlaceholderEntity = this.placeholderRepository.findById(2l).get();
		long idPlaceHolder = govioPlaceholderEntity.getId();

		String json = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 1)
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
				.andExpect(jsonPath("$.position", is(1)))
				.andReturn();

		JsonReader reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		JsonObject si = reader.readObject();
		long id = si.getInt("placeholder_id");

		List<GovioTemplatePlaceholderEntity> findAll = this.templatePlaceholderRepository.findAll();

		GovioTemplatePlaceholderEntity govioTemplatePlaceholderEntity = findAll.stream().filter(p -> p.getId().getGovioPlaceholder().longValue() == id).collect(Collectors.toList()).get(0);

		assertEquals(id, govioTemplatePlaceholderEntity.getId().getGovioPlaceholder());
		assertEquals(si.getBoolean("mandatory"), govioTemplatePlaceholderEntity.isMandatory());
		assertEquals(si.getInt("position"), govioTemplatePlaceholderEntity.getPosition());


		govioPlaceholderEntity = this.placeholderRepository.findById(3l).get();
		idPlaceHolder = govioPlaceholderEntity.getId();

		json = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 2)
				.build()
				.toString();

		result = this.mockMvc.perform(post(PLACEHOLDERS_BASE_PATH, idTemplate1).queryParam(Costanti.USERS_QUERY_PARAM_PLACEHOLDER_ID, ""+idPlaceHolder)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.placeholder_id").isNumber())
				.andExpect(jsonPath("$.mandatory", is(false)))
				.andExpect(jsonPath("$.position", is(2)))
				.andReturn();

		reader = Json.createReader(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()));
		si = reader.readObject();
		long id2 = si.getInt("placeholder_id");

		findAll = this.templatePlaceholderRepository.findAll();

		govioTemplatePlaceholderEntity = findAll.stream().filter(p -> p.getId().getGovioPlaceholder().longValue() == id2).collect(Collectors.toList()).get(0);
		
		// place holder presenti ora: ph2 in posizione 1 mandatory=true,  ph2 in posizione 2 mandatory=true
		// scambio i placeholder indicando la stessa posizione per entrambi
		
		JsonObject obj1 = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 1)
				.add("placeholder_id", 2)
				.build();
		
		JsonObject obj2 = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 1)
				.add("placeholder_id", 1)
				.build();
		
		JsonArray array = Json.createArrayBuilder()
				.add(obj1)
				.add(obj2)
				.build();
		json = Json.createObjectBuilder()
				.add("items", array)
				.build()
				.toString();
		
		this.mockMvc.perform(put(PLACEHOLDERS_BASE_PATH, idTemplate1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail", is("Duplicated placeholder position: [1]")))
				.andReturn();
		
		// scambio i placeholder indicando lo stesso placeholder per le due posizioni
		
		obj1 = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 1)
				.add("placeholder_id", 1)
				.build();
		
		obj2 = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 2)
				.add("placeholder_id", 1)
				.build();
		
		array = Json.createArrayBuilder()
				.add(obj1)
				.add(obj2)
				.build();
		json = Json.createObjectBuilder()
				.add("items", array)
				.build()
				.toString();
		
		this.mockMvc.perform(put(PLACEHOLDERS_BASE_PATH, idTemplate1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status", is(422)))
				.andExpect(jsonPath("$.title", is("Unprocessable Entity")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail", is("Duplicated placeholder_id: [1] for template_id [1]")))
				.andReturn();
	}
	
	@Test
	void UC_13_03_PatchPlaceHolderFail_TemplateNotFound() throws Exception {
		long idTemplate1 = 10000;

		JsonObject obj1 = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 1)
				.add("placeholder_id", 2)
				.build();
		
		JsonObject obj2 = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 2)
				.add("placeholder_id", 1)
				.build();
		
		JsonArray array = Json.createArrayBuilder()
				.add(obj1)
				.add(obj2)
				.build();
		String json = Json.createObjectBuilder()
				.add("items", array)
				.build()
				.toString();
		
		this.mockMvc.perform(put(PLACEHOLDERS_BASE_PATH, idTemplate1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf())
				.content(json)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
		
	}
	
	@Test
	void UC_13_04_PatchPlaceHolderFail_TemplateInvalidID() throws Exception {
		String idTemplate1 = "XXX";

		JsonObject obj1 = Json.createObjectBuilder()
				.add("mandatory", false)
				.add("position", 1)
				.add("placeholder_id", 2)
				.build();
		
		JsonObject obj2 = Json.createObjectBuilder()
				.add("mandatory", true)
				.add("position", 2)
				.add("placeholder_id", 1)
				.build();
		
		JsonArray array = Json.createArrayBuilder()
				.add(obj1)
				.add(obj2)
				.build();
		String json = Json.createObjectBuilder()
				.add("items", array)
				.build()
				.toString();
		
		this.mockMvc.perform(put(PLACEHOLDERS_BASE_PATH, idTemplate1)
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

}

