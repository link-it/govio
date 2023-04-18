package it.govio.batch.test.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.h2.tools.Server;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.CannotCreateTransactionException;

import it.govio.batch.config.FileProcessingJobConfig;
import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.service.GovioBatchService;
import it.govio.batch.test.utils.GovioMessageBuilder;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class FileProcessingBrokenDbTest {

	@Autowired
	private GovioServiceInstancesRepository govioServiceInstancesRepository;

	@Autowired
	private GovioFilesRepository govioFilesRepository;

	@Autowired
	private GovioFilesRepository govioFileMessagesRepository;
	
	@Autowired
	private GovioMessagesRepository govioMessagesRepository;

	@Autowired
	@Qualifier(value = FileProcessingJobConfig.FILEPROCESSING_JOBNAME)
	private Job job;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private GovioBatchService govioBatchService;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	Logger log = LoggerFactory.getLogger(FileProcessingInterruptedJobTest.class);
	
	@BeforeEach
	void setUp(){
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();
	}
	
	public static void stopH2Database() throws SQLException {
        Connection conn = DriverManager
                .getConnection("jdbc:h2:file:/tmp/govio-batch-db", "sa", "");
         Statement stat = conn.createStatement();
         stat.executeUpdate("SHUTDOWN");
         stat.close();
         conn.close();
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
	 * promoteProcessingFileListener e riavviamo l'esecuzione, (Creando una nuova JobExecution)
	 * 
	 */
	//@Test
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
		
         
        //Thread.sleep(1000);

         // NOTA: Ricreare la connessione non serve, ci pensa spring batch in automatico, basta fare la sleep
    /*    conn = DriverManager
                .getConnection("jdbc:h2:file:/tmp/govio-batch-db", "sa", "");
        stat = conn.createStatement();
        ResultSet result = stat.executeQuery("SELECT * from govio_files");
        this.log.info(result.toString());*/
        
		// Vado di esecuzione asincrona 

		//this.jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<JobExecution> fu = executor.submit( () -> {
			try {
				return govioBatchService.runFileProcessingJob();
			} catch (CannotCreateTransactionException e) {
				// Se il db è andato giù, restituiamo null
				return null;
			}
		});

		this.log.info("Stopping H2 Database...");
		Thread.sleep(800);
		stopH2Database();
		
		this.log.info("Mi assicuro che il Job abbia sollevato un'eccezione del DB");
		JobExecution jobExecution = fu.get();
		Assert.assertEquals(jobExecution, null);
		
		// Rilancio il jobbe
		this.log.info("Rilancio il Job..");
		Thread.sleep(100000);		// Attendo perchè il db torni su.
		fu = executor.submit( () -> {
			return govioBatchService.runFileProcessingJob();
		});
		
		jobExecution = fu.get();
		Thread.sleep(300);
		
	/*	while (!jobExecution.isRunning()) {
			jobExecution = this.jobExplorer.getJobExecution(jobExecution.getId());
		}
		
		while(jobExecution.isRunning()) {
			this.log.info("Waiting for the execution to end...");
			jobExecution = this.jobExplorer.getJobExecution(jobExecution.getId());
		}*/
	
		
		//jobExecution = this.jobRepository.getLastJobExecution(jobExecution.getJobInstance().getJobName(), jobExecution.getJobParameters());
		
		this.log.info("Job status: {}", jobExecution.getExitStatus());
		
		
		//Assert.assertEquals("STOPPED", jobExecution.getExitStatus().getExitCode());
		
		// Rilancio l'esecuzione, che deve tenere conto di precedenti istanze
		// jobExecution = govioBatchService.runFileProcessingJob();
		
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

}
