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
package it.govio.batch.test.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.GovioMessageEntityBuilder;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFileMessagesRepository;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.pagopa.io.v1.api.beans.ExternalMessageResponseWithContent;
import it.pagopa.io.v1.api.beans.MessageStatusValue;
import it.pagopa.io.v1.api.impl.ApiClient;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class VerifyMessagesJobTest {

	@Mock
	private RestTemplate restTemplate;

	@Autowired
	@InjectMocks
	private ApiClient apiClient;

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioFilesRepository govioFilesRepository;
	
	@Autowired
	private GovioFileMessagesRepository govioFileMessagesRepository;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier(value = "VerifyMessagesJob")
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRepository jobRepository;

	private void initailizeJobLauncherTestUtils() throws Exception { 
	    this.jobLauncherTestUtils = new JobLauncherTestUtils();
	    this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
	    this.jobLauncherTestUtils.setJobRepository(jobRepository);
	    this.jobLauncherTestUtils.setJob(job);
	}

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll(); 
	}

	@Test
	void verifyMessagesOk() throws Exception {

		// Caricamento messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		
		List<GovioMessageEntity> messages = new ArrayList<>();
		
		for(int i=0; i<100; i++) {
			GovioMessageEntityBuilder messageBuilder = GovioMessageEntity.builder()
					.govioServiceInstance(serviceInstanceEntity.get())
					.govhubUserId(1l)
					.markdown("Lorem Ipsum")
					.subject("Subject")
					.taxcode(String.format("%03d", i) + "AAA00A00A000A")
					.scheduledExpeditionDate(LocalDateTime.now().minusDays(1))
					.creationDate(LocalDateTime.now().minusDays(2))
					.expeditionDate(LocalDateTime.now().minusDays(1))
					.appioMessageId(UUID.randomUUID().toString());
					
			switch (i%3) {
			case 0:
				messageBuilder.status(Status.SENT);
				break;
			case 1:
				messageBuilder.status(Status.THROTTLED);
				break;
			case 2:
				messageBuilder.status(Status.ACCEPTED);
				break;
			}
			GovioMessageEntity message = messageBuilder.build();
			messages.add(govioMessagesRepository.save(message));
		}
		
		Mockito
		.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<ExternalMessageResponseWithContent>() {})))
		.thenAnswer(new Answer<ResponseEntity<ExternalMessageResponseWithContent>>() {
			@Override
			public ResponseEntity<ExternalMessageResponseWithContent> answer(InvocationOnMock invocation) throws Exception{
//				java.util.Random r = new java.util.Random();
//				int nextInt = r.nextInt(400);
//				// Simulazione ritardo chiamata http
//				Thread.sleep(nextInt+100);
				ExternalMessageResponseWithContent response = new ExternalMessageResponseWithContent();
				response.setStatus(MessageStatusValue.PROCESSED);
				return new ResponseEntity<ExternalMessageResponseWithContent>(response, HttpStatus.OK);
			}
		});

		Mockito
		.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());

		initailizeJobLauncherTestUtils();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

		//Controllo che tutti i messaggi siano spediti con successo e aggiornati conseguentemente.

		List<GovioMessageEntity> findAll = govioMessagesRepository.findAll();
		for(GovioMessageEntity entity : findAll) {
			Assert.assertNotNull(entity.getExpeditionDate());
			Assert.assertNotNull(entity.getLastUpdateStatus());
			Assert.assertEquals(Status.PROCESSED, entity.getStatus());
			Assert.assertNotNull(entity.getAppioMessageId());
		}
	}


}
