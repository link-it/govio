package it.govio.batch.test.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.h2.tools.Server;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import it.govio.batch.Application;
import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.test.config.FileProcessingJobInterruptedTestConfig;
import it.govio.batch.test.utils.GovioMessageBuilder;

@SpringBootTest
@Import(FileProcessingJobInterruptedTestConfig.class)
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class FileProcessingInterruptedJobTest {

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioFilesRepository govioFilesRepository;

	@Autowired
	private GovioFilesRepository govioFileMessagesRepository;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;

	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	@Qualifier(value = "FileProcessingJob")
	private Job job;

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobOperator jobOperator;
	
	Logger log = LoggerFactory.getLogger(FileProcessingInterruptedJobTest.class);

	@BeforeAll
	public void initTest() throws SQLException {
		Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082")
		.start();
	}

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();
	}

	/**
	 * Test di elaborazione tracciato a metà: Interrompiamo l'esecuzione del batch non appena finisce la
	 * promoteProcessingFileListener e riavviamo l'esecuzione, (Creando una nuova JobExecution)
	 * 
	 */
	@Test
	void csvLoadInterruptedAndRestartedNew() throws Exception {
		
		assertEquals(0, govioMessagesRepository.count());
		
		// Caricamento messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);

		TemporaryFolder testFolder = new TemporaryFolder();
		testFolder.create();

		// Inserisco 5 file con 100 record ciascuna
		List<GovioFileEntity> files = new ArrayList<>();
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "01")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "02")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "03")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "04")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "05")));

		this.jobLauncherTestUtils = new JobLauncherTestUtils();
		
		//this.jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
		this.jobLauncherTestUtils.setJobRepository(jobRepository);
		this.jobLauncherTestUtils.setJob(job);
		
		// Vado di esecuzione sincrona, ci pensa il listener a bloccare il job.
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		
		Assert.assertEquals("STOPPED", jobExecution.getExitStatus().getExitCode());
		
		jobExecution = jobLauncherTestUtils.launchJob();
		
		// Dopo la prima volta il listener non verrà più eseguito e mi aspetto che il job completi
		
		this.log.info("Checking Job Execution Exit Code...");
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

		for(GovioFileEntity entity : govioFilesRepository.findAll()) {
			// Controllo lo stato di elaborazione
			assertEquals(Status.PROCESSED, entity.getStatus());
			assertEquals(100, entity.getAcquiredMessages());
			assertEquals(0, entity.getErrorMessages());
		}

		assertEquals(500, govioMessagesRepository.count());

		for(GovioMessageEntity entity : govioMessagesRepository.findAll()) {
			assertEquals(GovioMessageEntity.Status.SCHEDULED, entity.getStatus());
		}
		
	}
	
	
	/**
	 * Test di elaborazione tracciato a metà: Interrompiamo l'esecuzione del batch non appena finisce la
	 * promoteProcessingFileListener e riavviamo l'esecuzione, (Senza crearne una nuova, quindi passando per una nuova
	 * JobIstance ma stessa JobExecution.
	 * 
	 */
	@Test
	void csvLoadInterruptedAndRestarted() throws Exception {
		
		assertEquals(0, govioMessagesRepository.count());
		
		// Caricamento messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);

		TemporaryFolder testFolder = new TemporaryFolder();
		testFolder.create();

		// Inserisco 5 file con 100 record ciascuna
		List<GovioFileEntity> files = new ArrayList<>();
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "01")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "02")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "03")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "04")));
		files.add(govioFilesRepository.save(GovioMessageBuilder.buildFile(testFolder, serviceInstanceEntity.get(), "05")));

		this.jobLauncherTestUtils = new JobLauncherTestUtils();
		
		//this.jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		this.jobLauncherTestUtils.setJobLauncher(jobLauncher);
		this.jobLauncherTestUtils.setJobRepository(jobRepository);
		this.jobLauncherTestUtils.setJob(job);
		
		// Vado di esecuzione sincrona, ci pensa il listener a bloccare il job.
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();
		
		Assert.assertEquals("STOPPED", jobExecution.getExitStatus().getExitCode());
		
		this.jobOperator.restart(jobExecution.getJobInstance().getInstanceId());
		
		var lastExec = this.jobRepository.getLastJobExecution(jobExecution.getJobInstance().getJobName(), jobExecution.getJobParameters());
		
		// Dopo la prima volta il listener non verrà più eseguito e mi aspetto che il job completi
		
		this.log.info("Checking Job Execution Exit Code...");
		Assert.assertEquals("COMPLETED", lastExec.getExitStatus().getExitCode());

		for(GovioFileEntity entity : govioFilesRepository.findAll()) {
			// Controllo lo stato di elaborazione
			assertEquals(Status.PROCESSED, entity.getStatus());
			assertEquals(100, entity.getAcquiredMessages());
			assertEquals(0, entity.getErrorMessages());
		}

		assertEquals(500, govioMessagesRepository.count());

		for(GovioMessageEntity entity : govioMessagesRepository.findAll()) {
			assertEquals(GovioMessageEntity.Status.SCHEDULED, entity.getStatus());
		}

	}
	
}
