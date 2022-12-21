package it.govhub.govio.api.test.controller.trace;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

import it.govhub.govio.api.Application;
import it.govhub.govio.api.test.utils.UserAuthProfilesUtils;


@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@DisplayName("Test di censimento Utenti")
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)

class Trace_UC_1_UploadCsvTracesTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private UserAuthProfilesUtils userAuthProfilesUtils;
	
	@Test
	void UC_1_01_RunOk() throws Exception {
		
	}
}
