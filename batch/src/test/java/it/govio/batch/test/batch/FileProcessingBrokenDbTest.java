/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
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

import static it.govio.batch.config.FileProcessingJobConfig.FILEPROCESSING_JOB;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioFileEntity.Status;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.service.GovioBatchService;
import it.govio.batch.test.utils.DBUtils;
import it.govio.batch.test.utils.GovioMessageBuilder;

@SpringBootTest(properties = {
		"jobs.FileProcessingJob.steps.govioFileReaderMasterStep.partitioner.grid-size=10",
		"jobs.FileProcessingJob.steps.loadCsvFileToDbStep.executor.max-pool-size:10"
})
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class FileProcessingBrokenDbTest {

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
	
	Logger log = LoggerFactory.getLogger(FileProcessingInterruptedJobTest.class);
	
	static final int FILE_COUNT = 30;
	static final int RECORDS_PER_FILE = 5000;
	
	@BeforeEach
	void setUp() throws IOException{
		govioFileMessagesRepository.deleteAll();
		govioFilesRepository.deleteAll();
		govioMessagesRepository.deleteAll();
		
		assertEquals(govioMessagesRepository.count(), 0);

		// Caricamento Files di messaggi da inviare
		
		Optional<GovioServiceInstanceEntity> serviceInstanceEntity = govioServiceInstancesRepository.findById(1L);
		TemporaryFolder testFolder = new TemporaryFolder();
		testFolder.create();
		
		List<GovioFileEntity> files = new ArrayList<>();
		
		for (int i = 0; i < FILE_COUNT; i++) {
			files.add(govioFilesRepository.save(
					GovioMessageBuilder.buildFileWithUniqueCF(
							testFolder, 
							serviceInstanceEntity.get(),
							i,
							RECORDS_PER_FILE))); 
		}
	}
	
	@Test
	void csvLoadInterruptedAndRestartedNew() throws Exception {
		
		assertEquals(0, govioMessagesRepository.count());
		
		final int fileProcessingSleepBeforeShutdown = 500;

		int count = 30;
		while (count > 0) {
			final Future<JobExecution> futureBrokenJob = this.runFileProcessingJobAsync();
			
			this.log.info("Lascio lavorare il Job [{}] per {}ms...", FILEPROCESSING_JOB, fileProcessingSleepBeforeShutdown);
			Thread.sleep(fileProcessingSleepBeforeShutdown);
			
			this.log.info("Stopping H2 Database...");
			DBUtils.stopH2Database();
			
			this.log.info("Mi assicuro che il Job [{}] abbia sollevato un'eccezione del DB", FILEPROCESSING_JOB);
			
			final JobExecution brokenExecution = futureBrokenJob.get();
			if (brokenExecution != null) {
				this.log.info("Il Job [{}] è rimasto in stato {}", FILEPROCESSING_JOB, brokenExecution.getStatus());
				Assert.assertTrue(BatchStatus.UNKNOWN == brokenExecution.getStatus() || BatchStatus.FAILED == brokenExecution.getStatus());
			}
			
			this.log.info("Attendo che il db si riprenda");
			DBUtils.awaitForDb(jobExplorer, FILEPROCESSING_JOB);
			
			this.log.info("Provo Rieseguire il job, mi aspetto che non venga avviato vista la precedente terminazione anormale");
			final JobExecution notStartedExecution = this.govioBatchService.runFileProcessingJob();
			Assert.assertEquals(null, notStartedExecution);
			
			final JobInstance instanceToAbandon = this.jobExplorer.getLastJobInstance(FILEPROCESSING_JOB);
			final JobExecution executionToAbandon = 	this.jobExplorer.getLastJobExecution(instanceToAbandon);
			
			this.log.info("Il Job [{}] è rimasto in stato {}", FILEPROCESSING_JOB, executionToAbandon.getStatus());
			this.log.info("Aggiorno lo stato dell'ultimo job ad Abandoned");
	
			Assert.assertNotEquals(BatchStatus.ABANDONED, executionToAbandon.getStatus());
			Assert.assertNotEquals(BatchStatus.COMPLETED, executionToAbandon.getStatus());
			Assert.assertNotEquals(BatchStatus.FAILED, executionToAbandon.getStatus());
			Assert.assertNotEquals(BatchStatus.STOPPED, executionToAbandon.getStatus());
			
			this.jobOperator.stop(executionToAbandon.getId());
			this.jobOperator.abandon(executionToAbandon.getId());
			
			final JobExecution executionAbandoned =	this.jobExplorer.getLastJobExecution(instanceToAbandon);
			Assert.assertEquals(BatchStatus.ABANDONED, executionAbandoned.getStatus());
			count--;
		}
		
		this.log.info("Provo Rieseguire il job per intero, adesso deve riavviarsi perchè il precedente è abbandonato.");
		JobExecution completedFileProcessingExecution = this.govioBatchService.runFileProcessingJob();
		
		this.log.info("Job [{}] Terminato con ExitStatus [{}]", FILEPROCESSING_JOB, completedFileProcessingExecution.getExitStatus());
		Assert.assertEquals(BatchStatus.COMPLETED, completedFileProcessingExecution.getStatus());
		
		this.log.info("Eseguo i restanti job in modo da elaborare il resto dei files.");
		for(int i=0; i< FILE_COUNT/10; i++) {
			completedFileProcessingExecution = this.govioBatchService.runFileProcessingJob();
			this.log.info("Job [{}] Terminato con ExitStatus [{}]", FILEPROCESSING_JOB, completedFileProcessingExecution.getExitStatus());
			Assert.assertEquals(BatchStatus.COMPLETED, completedFileProcessingExecution.getStatus());
		}
		
		for(GovioFileEntity entity : govioFilesRepository.findAll()) {
			// Controllo lo stato di elaborazione
			assertEquals(Status.PROCESSED, entity.getStatus());
			assertEquals(RECORDS_PER_FILE, entity.getAcquiredMessages());
			assertEquals(0, entity.getErrorMessages());
		}

		assertEquals(RECORDS_PER_FILE*FILE_COUNT, govioMessagesRepository.count());

		for(GovioMessageEntity entity : govioMessagesRepository.findAll()) {
			assertEquals(GovioMessageEntity.Status.SCHEDULED, entity.getStatus());
		}
	}
	
	private Future<JobExecution> runFileProcessingJobAsync() {
		return executor.submit( () -> {
			try {
				return govioBatchService.runFileProcessingJob();
			} catch (CannotCreateTransactionException | TransactionSystemException e) {
				// Se il db è andato giù, restituiamo null
				return null;
			}
		});
	}
	

}
