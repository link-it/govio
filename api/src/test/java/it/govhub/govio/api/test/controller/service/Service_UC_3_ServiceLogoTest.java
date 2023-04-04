/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
package it.govhub.govio.api.test.controller.service;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.costanti.Costanti;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("lettura logo e logo_miniature di un Service")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)

class Service_UC_3_ServiceLogoTest {

	private static final String SERVICES_BASE_PATH = "/v1/services";
	private static final String SERVICES_BASE_PATH_DETAIL_ID = SERVICES_BASE_PATH + "/{id}";
	private static final String SERVICES_BASE_PATH_LOGO = SERVICES_BASE_PATH_DETAIL_ID + "/logo";
	private static final String SERVICES_BASE_PATH_LOGO_MINIATURE = SERVICES_BASE_PATH_DETAIL_ID + "/logo-miniature";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ReadServiceRepository serviceRepository;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	private ServiceEntity leggiServizioDB(String nome) {
		List<ServiceEntity> findAll = this.serviceRepository.findAll();
		return findAll.stream().filter(f -> f.getName().equals(nome)).collect(Collectors.toList()).get(0);
	}
	
	@Test
	void UC_3_01_Organization_GetLogoMiniature() throws Exception {
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		Long id = servizio.getId();
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH_LOGO_MINIATURE, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
				.andReturn();
		
	}
	
	@Test
	void UC_3_02_Organization_GetLogo() throws Exception {
		ServiceEntity servizio = leggiServizioDB(Costanti.SERVICE_NAME_SERVIZIO_GENERICO);
		Long id = servizio.getId();

		this.mockMvc.perform(get(SERVICES_BASE_PATH_LOGO, id)
				.with(this.userAuthProfilesUtils.utenzaAdmin())
				.accept(MediaType.APPLICATION_OCTET_STREAM))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
				.andReturn();
		
	}
	
	@Test
	void UC_3_03_Organization_LogoMiniature_OrganizationNotFound() throws Exception {
		int idUser1 = 10000;
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH_LOGO_MINIATURE,idUser1)
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
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH_LOGO_MINIATURE,idUser1)
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
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH_LOGO,idUser1)
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
		
		this.mockMvc.perform(get(SERVICES_BASE_PATH_LOGO,idUser1)
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
