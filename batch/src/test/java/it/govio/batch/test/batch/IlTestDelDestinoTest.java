package it.govio.batch.test.batch;

import static it.govio.batch.config.FileProcessingJobConfig.FILEPROCESSING_JOB;
import static it.govio.batch.test.utils.GovioMessageBuilder.buildFile;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.CannotCreateTransactionException;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.service.GovioBatchService;
import it.govio.batch.test.config.JobOperatorConfig;
import it.govio.batch.test.utils.DBUtils;

@SpringBootTest
@RunWith(SpringRunner.class)
@Import({ JobOperatorConfig.class})
@EnableAutoConfiguration
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class IlTestDelDestinoTest {

	@Autowired
	GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	GovioFilesRepository govioFilesRepository;

	@Autowired
	GovioFilesRepository govioFileMessagesRepository;

	@Autowired
	GovioMessagesRepository govioMessagesRepository;

	@Autowired
	JobExplorer jobExplorer;

	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobOperator jobOperator;

	@Autowired
	GovioBatchService govioBatchService;

	@Autowired
	JobLauncher jobLauncher;
	
	ExecutorService executor = Executors.newSingleThreadExecutor();

	static final int FILE_COUNT = 5;
	static final int RECORDS_PER_FILE = 100;

	Logger log = LoggerFactory.getLogger(IlTestDelDestinoTest.class);

	List<GovioFileEntity> setUp() throws IOException {
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();

		assertEquals(0, govioMessagesRepository.count());

		// Caricamento Files di messaggi da inviare
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		TemporaryFolder testFolder = new TemporaryFolder();
		testFolder.create();
		
		// TODO: SE NE GENERO TANTI COL CICLO FOR FALLISCE; DOPO VEDI COME MAI.
		List<GovioFileEntity> files = new ArrayList<>();
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "01", 1000)));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "02", 1000)));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "03", 1000)));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "04", 1000)));
		files.add(govioFilesRepository.save(buildFile(testFolder, serviceInstanceEntity.get(), "05", 1000)));
		
		/*for (int i = 0; i < RECORDS_PER_FILE; i++) {
			files.add(govioFilesRepository.save(
					buildFile(
							testFolder, 
							serviceInstanceEntity.get(),
							String.format("%02d", i)))
					); 
		}*/

		return files;
	}

	
	// TODO: Se ci metto un solo codice fiscale errato, tutto il job va a puttane.
	
	//@Test
	public void ilTestDelDestino2() throws Exception {
		for(int i=0;i<10;i++) {
			this.log.info("RUN NUMERO {}", i);
			ilTestDelDestino();
		}
	}
	
	@Test
	public void ilTestDelDestino() throws Exception{
		List<GovioFileEntity> files = setUp();
		
		int sleepTime = 800;

		Future<JobExecution> fj = this.runFileProcessingJobAsync();
		
		this.log.info("Lascio lavorare il Job [{}] per {}ms...", FILEPROCESSING_JOB, sleepTime);
		Thread.sleep(sleepTime);
		
		this.log.info("Stopping H2 Database...");
		DBUtils.stopH2Database();
		
		this.log.info("Mi assicuro che il Job [{}] abbia sollevato un'eccezione del DB", FILEPROCESSING_JOB);
		JobExecution jobExecution = fj.get();
		Assert.assertEquals(null, jobExecution);
		
		this.log.info("Attendo che il db si riprenda");
		DBUtils.awaitForDb(jobExplorer, FILEPROCESSING_JOB);
		
		// TODO SE LASCIO QUESTO (CON maxPoolSize=10), L'EXIT STATUS DELL'ULTIMA EXECUTION  È COMPLETO DI STACK TRACE, ALTRIMENTI NON LA VEDO, ANCHE SE FALLISCE PER LO STESSO MOTIVO.
		//DBUtils.clearSpringBatchTables();	
		
		this.log.info("Provo Rieseguire il job, mi aspetto che sia fallito vista la terminazione anormale");
		fj = this.runFileProcessingJobAsync();
		jobExecution = fj.get();
		Assert.assertEquals(null, jobExecution);
		
		JobInstance instanceToAbandon = this.jobExplorer.getLastJobInstance(FILEPROCESSING_JOB);
		JobExecution executionToAbandon = 	this.jobExplorer.getLastJobExecution(instanceToAbandon);
		
		this.log.info("Il Job [{}] è rimasto in stato {}", FILEPROCESSING_JOB, executionToAbandon.getStatus());
		this.log.info("Aggiorno lo stato dell'ultimo job ad Abandoned");

		Assert.assertNotEquals(BatchStatus.ABANDONED, executionToAbandon.getStatus());
		Assert.assertNotEquals(BatchStatus.COMPLETED, executionToAbandon.getStatus());
		Assert.assertNotEquals(BatchStatus.FAILED, executionToAbandon.getStatus());
		Assert.assertNotEquals(BatchStatus.STOPPED, executionToAbandon.getStatus());
		
		this.jobOperator.stop(executionToAbandon.getId());
		this.jobOperator.abandon(executionToAbandon.getId());
		
		
		JobExecution executionAbandoned =	this.jobExplorer.getLastJobExecution(instanceToAbandon);
		Assert.assertEquals(BatchStatus.ABANDONED, executionAbandoned.getStatus());
		
		
		this.log.info("Provo Rieseguire il job per intero, adesso deve riavviarsi perchè il precedente è abbandonato.");
		JobExecution completedFileProcessingExecution = this.govioBatchService.runFileProcessingJob();
		
		this.log.info("Job [{}] Terminato con ExitStatus [{}]", FILEPROCESSING_JOB, completedFileProcessingExecution.getExitStatus());
		Assert.assertEquals(BatchStatus.COMPLETED, completedFileProcessingExecution.getStatus());

		
		// TODO this.updateJobExecutionStatus(jobExecution, BatchStatus.ABANDONED);
		
/*		this.govioBatchService.runSendMessageJob();
		
		
		this.govioBatchService.runVerifyMessagesJob();*/
	
	}
	
	
	private Future<JobExecution> runFileProcessingJobAsync() {
		return executor.submit( () -> {
			try {
				return govioBatchService.runFileProcessingJob();
			} catch (CannotCreateTransactionException e) {
				// Se il db è andato giù, restituiamo null
				return null;
			}
		});
	}
	
	
	
}
