package it.govio.batch.test.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import it.govio.batch.Application;
import it.govio.batch.config.FileProcessingJobConfig;
import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.test.config.FileProcessingJobInterruptedTestConfig;
import it.govio.batch.test.utils.GovioMessageBuilder;


/**
 * Importiamo gli hook che stoppano il job non appena il primo step "promoteProcessingFileTasklet" è terminato.
 * A questo punto, lo scheduler dovrebbe riavviare l'esecuzione dopo X secondi e riprendere la stessa job instance.
 * 
 * DISABILITO (POI CANCELLO SE NECESSARIO) QUESTO TEST, PERCHÈ SE PARTE LO SCHEDULING, POI RESTA VIVO
 * ATTRAVERSO LE VARIE CLASSI.
 * 
 */

@SpringBootTest(
			classes = Application.class,
			properties = {"scheduler.initialDelayString=1000"}
		)
@Import(FileProcessingJobInterruptedTestConfig.class)
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class SchedulingFileProcessingJobInterruptedTest {
	
	
	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioFilesRepository govioFilesRepository;

	@Autowired
	private GovioFilesRepository govioFileMessagesRepository;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private JobOperator jobOperator;
	
	Logger log = LoggerFactory.getLogger(SchedulingFileProcessingJobInterruptedTest.class);

	@BeforeEach
	void setUp(){
		MockitoAnnotations.openMocks(this);
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();
	}
	
	@AfterEach
	void cleanUp() throws NoSuchJobExecutionException, InterruptedException {
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();
		
		for (String jn : jobExplorer.getJobNames()) {             
			   for (JobExecution je : jobExplorer.findRunningJobExecutions(jn)) {
				   try {
					   this.jobOperator.stop(je.getId());
				   } catch (JobExecutionNotRunningException e) {
				   }
			    }
		}
		
		awaitAllCurrentJobs();
	}
	
	public void awaitAllCurrentJobs() throws InterruptedException {
		
		for (String jn : jobExplorer.getJobNames()) {             
			   for (JobExecution je : jobExplorer.findRunningJobExecutions(jn)) {
				   while (je.isRunning()) {
						je = this.jobExplorer.getJobExecution(je.getId());
						Thread.sleep(20);
				   }
			   }
		}
		
	}

	/**
	 * Test di elaborazione tracciato a metà: Interrompiamo l'esecuzione del batch non appena finisce la
	 * promoteProcessingFileListener e controlliamo che lo scheduler riprenda correttamente il lavoro.
	 * 
	 * @throws InterruptedException 
	 * @throws IOException 
	 * 
	 */
	//@Test
	public void interruptJobThenResumedByScheduler() throws InterruptedException, IOException {
		
		// Popolo il Db...	
		List<GovioFileEntity> files = populateDb();
		
		// Attendo per l'esecuzione del job...

		this.log.info("Job starting...");
		Set<JobExecution> executions = this.jobExplorer.findRunningJobExecutions(FileProcessingJobConfig.FILEPROCESSING_JOBNAME);
		while (executions.size() == 0) {
			executions = this.jobExplorer.findRunningJobExecutions(FileProcessingJobConfig.FILEPROCESSING_JOBNAME);
			Thread.sleep(20);
		}
		
		assertEquals(executions.size(), 1);
		
		JobExecution jobExecution = executions.stream().findFirst().get();
		Long instanceId = jobExecution.getJobInstance().getId();
		
		this.log.info("Waiting for job to stop...");
		while (jobExecution.isRunning()) {
				jobExecution = this.jobExplorer.getJobExecution(jobExecution.getId());
		}
		Assert.assertEquals("STOPPED", jobExecution.getExitStatus().getExitCode());

		// Qui potrei testare gli effetti del primo step. (TODO)	
		
		JobInstance instance = this.jobExplorer.getJobInstance(instanceId);
		
		jobExecution = this.jobExplorer.getLastJobExecution(instance);
		
		this.log.info("Waiting for job to automatically restart...");
		while (!jobExecution.isRunning()) {
			jobExecution = this.jobExplorer.getLastJobExecution(instance);
		}
		
		this.log.info("Waiting for job to end...");
		while (jobExecution.isRunning()) {
				jobExecution = this.jobExplorer.getJobExecution(jobExecution.getId());
		}

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
	
	
	private List<GovioFileEntity> populateDb() throws IOException {
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
		
		return files;
	}


}
