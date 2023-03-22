package it.govhub.govio.api.test.controller.organization;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

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
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento proprieta logo e logo_miniature di una Organization")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Organization_UC_3_OrganizationLogoTest {

	private static final String ORGANIZATIONS_BASE_PATH = "/v1/organizations";
	private static final String ORGANIZATIONS_BASE_PATH_DETAIL_ID = ORGANIZATIONS_BASE_PATH + "/{id}";
	private static final String ORGANIZATIONS_BASE_PATH_LOGO = ORGANIZATIONS_BASE_PATH_DETAIL_ID + "/logo";
	private static final String ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE = ORGANIZATIONS_BASE_PATH_DETAIL_ID + "/logo-miniature";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReadOrganizationRepository organizationRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private OrganizationEntity leggiEnteDB(String nome) {
		List<OrganizationEntity> findAll = this.organizationRepository.findAll();
		return findAll.stream().filter(f -> f.getTaxCode().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	@Test
	void UC_3_01_Organization_GetLogoMiniature() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		Long id = ente.getId();
		
		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andReturn();

		
	}
	
	@Test
	void UC_3_02_Organization_GetLogo() throws Exception {
		OrganizationEntity ente = leggiEnteDB(Costanti.TAX_CODE_ENTE_CREDITORE_3);
		Long id = ente.getId();

		MvcResult result = this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andReturn();
		
	}
	
	@Test
	void UC_3_03_Organization_LogoMiniature_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_3_04_Organization_LogoMiniature_OrganizationInvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test
	void UC_3_05_Organization_Logo_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH_LOGO,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.title", is("Not Found")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
	
	@Test	
	void UC_3_06_Organization_Logo_OrganizationInvalidId() throws Exception {
		String idUser1 = "XXX";
		
		this.mockMvc.perform(get(ORGANIZATIONS_BASE_PATH_LOGO,idUser1)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.with(csrf()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.title", is("Bad Request")))
				.andExpect(jsonPath("$.type").isString())
				.andExpect(jsonPath("$.detail").isString())
				.andReturn();
	}
}
