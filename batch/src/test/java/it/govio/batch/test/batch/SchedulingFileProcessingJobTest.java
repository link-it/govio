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
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import it.govio.batch.test.utils.GovioMessageBuilder;

/**
 * 
 * DISABILITO (POI CANCELLO SE NECESSARIO) QUESTO TEST, PERCHÈ SE PARTE LO SCHEDULING, POI RESTA VIVO
 * ATTRAVERSO LE VARIE CLASSI.
 * 
 */

@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class SchedulingFileProcessingJobTest {
	
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

	Logger log = LoggerFactory.getLogger(FileProcessingInterruptedJobTest.class);

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
	 * Lascia agli scheduler il compito di iniziare i jobs. Verifica che "FileProcessingJob" venga iniziato e portato a termine.
	 * 
	 */
	//@Test
	public void isSchedulingWorking() throws InterruptedException, IOException {
		
		// Popolo il Db...	
		populateDb();
		
		// Attendo per l'esecuzione del job...

		// TODO: Exponential backoff?
		this.log.info("Job starting...");
		Set<JobExecution> executions = this.jobExplorer.findRunningJobExecutions("FileProcessingJob");
		while (executions.size() == 0) {
			executions = this.jobExplorer.findRunningJobExecutions("FileProcessingJob");
		}
		
		assertEquals(executions.size(), 1);
		
		JobExecution jobExecution = executions.stream().findFirst().get();
		
		this.log.info("Waiting for job to finish...");
		while (jobExecution.isRunning()) {
				jobExecution = this.jobExplorer.getJobExecution(jobExecution.getId());
		}
		
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
		
	/*	for (String jn : jobExplorer.getJobNames())             
			   for (JobExecution je : jobExplorer.findRunningJobExecutions(jn)) {
			    }*/				
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
	
	/**
	 * Scheduling should use Preexisting Jobs.
	 * 
	 * Dato il modo in cui è scritto l'Application.java, dobbiamo accertarci che quando un job viene rieseguto, allora viene pescata
	 * un'istanza passata, nel caso questa sia "stopped"
	 *  
	 */
	/*void schedulingShouldUsePreexistingJobs() throws Exception {
		
	}*/

}
