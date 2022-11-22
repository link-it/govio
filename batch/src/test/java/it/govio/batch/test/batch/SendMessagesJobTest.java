package it.govio.batch.test.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.FiscalCodePayload;
import it.pagopa.io.v1.api.beans.LimitedProfile;
import it.pagopa.io.v1.api.impl.ApiClient;

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class SendMessagesJobTest {

	@Mock
	private RestTemplate restTemplate;

	@Autowired
	@InjectMocks
	private ApiClient apiClient;

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier(value = "SendMessagesJob")
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
	
	/**
	 * Database inizializzato con:
	 * - 100 messaggi schedulati
	 * - 100 messaggi in errore
	 * - 100 messaggi spediti
	 * - 100 messaggi 
	 * @throws Exception
	 */

	@BeforeEach
	void setUp(){
		 
		MockitoAnnotations.openMocks(this);
		govioMessagesRepository.deleteAll();
		
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);

		for(int i=0; i<100; i++) {
			GovioMessageEntity message = GovioMessageEntity.builder()
					.govioServiceInstance(serviceInstanceEntity.get())
					.markdown("Lorem Ipsum")
					.subject("Subject")
					.taxcode(String.format("%03d", i) + "AAA00A00A000A")
					.scheduledExpeditionDate(LocalDateTime.now().minusDays(1))
					.creationDate(LocalDateTime.now().minusDays(2))
					.status(Status.SCHEDULED)
					.build();
			govioMessagesRepository.save(message);
		}
	}


	@Test
	public void sendMessagesOk() throws Exception {

		Random r = new Random();

		Set<String> failedTaxCodes = new HashSet<>();

		Mockito
		.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenAnswer(new Answer<ResponseEntity<LimitedProfile>>() {
			@Override
			public ResponseEntity<LimitedProfile> answer(InvocationOnMock invocation) throws Exception{
				@SuppressWarnings("unchecked")
				String fiscalCode = ((RequestEntity<FiscalCodePayload>) invocation.getArgument(0)).getBody().getFiscalCode();
				int nextInt = r.nextInt(400);
				Thread.sleep(nextInt+100);
				
				// Nel 10% dei casi lancio una eccezione, se il codice fiscale non è già stato oggetto di eccezioni.
				if(nextInt < 40 && failedTaxCodes.contains(fiscalCode)) { 
					failedTaxCodes.add(fiscalCode);
					throw new IOException();
				}
				LimitedProfile profile = new LimitedProfile();
				profile.setSenderAllowed(true);
				return new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK);
			}
		});


		Mockito
		.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<CreatedMessage>() {})))
		.thenAnswer(new Answer<ResponseEntity<CreatedMessage>>() {
			@Override
			public ResponseEntity<CreatedMessage> answer(InvocationOnMock invocation) throws InterruptedException{
				Thread.sleep(r.nextInt(400)+100);
				CreatedMessage createdMessage = new CreatedMessage();
				createdMessage.setId(UUID.randomUUID().toString());
				return new ResponseEntity<CreatedMessage>(createdMessage, HttpStatus.CREATED);
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
			Assert.assertEquals(Status.SENT, entity.getStatus());
			Assert.assertNotNull(entity.getAppioMessageId());
		}
	}


	@Test
	public void sendMessagesFailure() throws Exception {

		Random r = new Random();
		Set<String> failedTaxCodes = new HashSet<>();

		Mockito
		.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<LimitedProfile>() {})))
		.thenAnswer(new Answer<ResponseEntity<LimitedProfile>>() {
			@Override
			public ResponseEntity<LimitedProfile> answer(InvocationOnMock invocation) throws Exception{
				@SuppressWarnings("unchecked")
				String fiscalCode = ((RequestEntity<FiscalCodePayload>) invocation.getArgument(0)).getBody().getFiscalCode();
				int nextInt = r.nextInt(400);
				Thread.sleep(nextInt+100);
				
				// Nel 10% dei casi lancio una eccezione, se il codice fiscale non è già stato oggetto di eccezioni.
				if(nextInt < 40 && failedTaxCodes.contains(fiscalCode)) { 
					failedTaxCodes.add(fiscalCode);
					throw new IOException();
				}
				LimitedProfile profile = new LimitedProfile();
				profile.setSenderAllowed(true);
				return new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK);
			}
		});


		Mockito
		.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<CreatedMessage>() {})))
		.thenAnswer(new Answer<ResponseEntity<CreatedMessage>>() {
			@Override
			public ResponseEntity<CreatedMessage> answer(InvocationOnMock invocation) throws InterruptedException{
				Thread.sleep(r.nextInt(400)+100);
				CreatedMessage createdMessage = new CreatedMessage();
				createdMessage.setId(UUID.randomUUID().toString());
				return new ResponseEntity<CreatedMessage>(createdMessage, HttpStatus.CREATED);
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
			Assert.assertEquals(Status.SENT, entity.getStatus());
			Assert.assertNotNull(entity.getAppioMessageId());
		}
	}
}
