package it.govio.batch.test.batch;

import static it.govio.batch.config.FileProcessingJobConfig.FILEPROCESSING_JOB;
import static it.govio.batch.config.SendMessagesJobConfig.SENDMESSAGES_JOB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
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
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import it.govio.batch.entity.GovioFileEntity;
import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.entity.GovioServiceInstanceEntity;
import it.govio.batch.repository.GovioFilesRepository;
import it.govio.batch.repository.GovioMessagesRepository;
import it.govio.batch.repository.GovioServiceInstancesRepository;
import it.govio.batch.service.GovioBatchService;
import it.govio.batch.test.config.EndToEndBrokenDbTestObservableConfig;
import it.govio.batch.test.config.JobOperatorConfig;
import it.govio.batch.test.utils.DBUtils;
import it.govio.batch.test.utils.GovioMessageBuilder;
import it.pagopa.io.v1.api.beans.CreatedMessage;
import it.pagopa.io.v1.api.beans.LimitedProfile;
import it.pagopa.io.v1.api.beans.NewMessage;
import it.pagopa.io.v1.api.impl.ApiClient;

/**
 * Test end to end del batch: FileProcessing -> SendMessages -> Verify Messages.

 * Durante l'esecuzione di ciascun job, tolgo il database da sotto il test, in modo che fallisca e finisca in uno stato inconsistente.
 * Poi vediamo cosa succede.
 * 
 *	 Nota: Tutte le variabili sono dichiarate come "final" per evitare bug nel test, dato che a livello di righe di codice è molto lungo.
 * Non affidandoci al riassegnare variabili, in ogni punto siamo sicuri di usare proprio quella jobExecution, quella jobIstance ecc...
 * 
 * Nota2: Dovrei testare esplicitamente nel metodo del test il fatto che la future sia fallita per un'eccezione del database.
 * Infatti adesso la future può restituire null in due casi, anche quando il relativo job lanciato restituisce null.
 * 
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Import({ JobOperatorConfig.class, EndToEndBrokenDbTestObservableConfig.class})
@EnableAutoConfiguration
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(MockitoExtension.class)
public class EndToEndBrokenDbTest {

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
	
	@Autowired
	EndToEndBrokenDbTestObservableConfig testListeners;
	
	@Mock
	RestTemplate restTemplate;
	
	@Autowired
	@InjectMocks
	ApiClient apiClient;
	
	ExecutorService executor = Executors.newSingleThreadExecutor();

	static final int FILE_COUNT = 40;
	static final int RECORDS_PER_FILE = 500;

	Logger log = LoggerFactory.getLogger(EndToEndBrokenDbTest.class);

	@BeforeEach
	private void  setUp() throws IOException {
		MockitoAnnotations.openMocks(this);

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
	public void ilTestDelDestino() throws Exception{
		buildMocks();

		final int fileProcessingSleepBeforeShutdown = 800;

		int count = 10;
		while (count > 0) {
			runFileProcessingJobThenBreakAndAbandonIt(fileProcessingSleepBeforeShutdown);
			count--;
		}
		
		this.log.info("Provo Rieseguire il job per intero, adesso deve riavviarsi perchè il precedente è abbandonato.");
		this.log.info("Eseguo i restanti job in modo da elaborare il resto dei files.");
		for(int i=0; i< FILE_COUNT/10; i++) {
			JobExecution completedFileProcessingExecution = this.govioBatchService.runFileProcessingJob();
			this.log.info("Job [{}] Terminato con ExitStatus [{}]", FILEPROCESSING_JOB, completedFileProcessingExecution.getExitStatus());
			Assert.assertEquals(BatchStatus.COMPLETED, completedFileProcessingExecution.getStatus());
		}
		
		this.log.info("Controllo che siano stati prodotti i GovioFile e i GovioMessage sul db...");
		for(GovioFileEntity entity : govioFilesRepository.findAll()) {
			assertEquals(GovioFileEntity.Status.PROCESSED, entity.getStatus());
			assertEquals(RECORDS_PER_FILE, entity.getAcquiredMessages());
			assertEquals(0, entity.getErrorMessages());
		}
		assertEquals(RECORDS_PER_FILE*FILE_COUNT, govioMessagesRepository.count());
		for(GovioMessageEntity entity : govioMessagesRepository.findAll()) {
			assertEquals(GovioMessageEntity.Status.SCHEDULED, entity.getStatus());
		}
		
		// Adesso ho terminato il primo job, dopo essersi ripreso da ripetuti errori gravi.
		
		// JOB - SENDMESSAGES
		// È composto da due step: GetProfile e NewMessage.				
		// Rompo il primo job n volte e poi aspetto di raggiungere il secondo step.
		this.log.info("TEST SendMessagesJob, lascio andare il primo step e lo rompo un pò di volte...");
		
		final int sleepBeforeShutdown = 500;
		BatchStatus status = null;		
		count = 5;
		while (count > 0) {
			status = runSendMessagesJobThenBreakIt(sleepBeforeShutdown)	;
			count--;
		}

		status = null;
		final Future<JobExecution> afterFirstStepResult = this.runSendMessageJobAsync();
		while ( !testListeners.getProfileDescriptor.isStepEnded() ) {
			this.log.info("TEST SendMessagesJob, attendo che il primo step sia finito..");		
			DBUtils.sleep(20);
		}
		this.log.info("Lo step getProfile è finito.");		
		
		// Non appena finisco il primo step, parto rompendo il secondo step.		
		this.log.info("Lascio lavorare il Job [{}] per {}ms...", SENDMESSAGES_JOB, sleepBeforeShutdown);
		DBUtils.sleep(sleepBeforeShutdown);
		this.log.info("Stopping H2 Database...");
		DBUtils.stopH2Database();
		this.log.info("Mi assicuro che il Job [{}] abbia sollevato un'eccezione del DB oppure che abbia completato.", SENDMESSAGES_JOB);
		final JobExecution afterFirstStepExecution = afterFirstStepResult.get();
		if (afterFirstStepExecution != null) {
			this.log.info("Il Job [{}] è rimasto in stato {}", SENDMESSAGES_JOB,afterFirstStepExecution);
			status = afterFirstStepExecution.getStatus();
		}
		this.log.info("Attendo che il db si riprenda");
		DBUtils.awaitForDb(jobExplorer, SENDMESSAGES_JOB);
		
		this.log.info("Adesso Rompo il DB {} volte durante il secondo step", 30);
		count = 30;
		final int sendMessagesSleepBeforeShutdown = 300;
		while (status != BatchStatus.COMPLETED && count > 0) {
			status = runSendMessagesJobThenBreakIt(sendMessagesSleepBeforeShutdown);
			count--;
		}
		
		JobExecution completedSendExecution = this.govioBatchService.runSendMessageJob();
		Assert.assertEquals(BatchStatus.COMPLETED, completedSendExecution.getStatus());
		
		List<GovioMessageEntity> findAll = govioMessagesRepository.findAll();
		for(GovioMessageEntity entity : findAll) {
			Assert.assertNotNull(entity.getExpeditionDate());
			Assert.assertNotNull(entity.getLastUpdateStatus());
			Assert.assertEquals(Status.SENT, entity.getStatus());
			Assert.assertNotNull(entity.getAppioMessageId());
		}
		
		Assert.assertEquals(FILE_COUNT*RECORDS_PER_FILE, findAll.size());
		
		if ( ! this.codiciFiscaliDuplicati.isEmpty()) {
			this.log.error("Trovati i seguenti codici fiscali duplicati!: {}", this.codiciFiscaliDuplicati);
		}

		Assert.assertEquals(FILE_COUNT*RECORDS_PER_FILE, messagesReceived.get());
	}


	private void runFileProcessingJobThenBreakAndAbandonIt(final int fileProcessingSleepBeforeShutdown) throws Exception{
		final Future<JobExecution> futureBrokenJob = this.runFileProcessingJobAsync();
		
		this.log.info("Lascio lavorare il Job [{}] per {}ms...", FILEPROCESSING_JOB, fileProcessingSleepBeforeShutdown);
		Thread.sleep(fileProcessingSleepBeforeShutdown);
		
		this.log.info("Stopping H2 Database...");
		DBUtils.stopH2Database();
		
		this.log.info("Mi assicuro che il Job [{}] abbia sollevato un'eccezione del DB", FILEPROCESSING_JOB);
		final JobExecution brokenExecution = futureBrokenJob.get();
		if (brokenExecution != null) {
			this.log.info("Il Job [{}] è rimasto in stato {}", FILEPROCESSING_JOB, brokenExecution.getStatus());
			
			// Se per caso il job ha finito di lavorare perchè la macchina è stata veloce, allora salta la parte di abandoning
			if (brokenExecution.getStatus() == BatchStatus.COMPLETED) {
				return;
			}
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
	}


	private BatchStatus runSendMessagesJobThenBreakIt(final int sleepBeforeShutdown)	throws SQLException, InterruptedException, ExecutionException {
		
		final Future<JobExecution> brokenSendMessage = this.runSendMessageJobAsync();
		
		this.log.info("Lascio lavorare il Job [{}] per {}ms...", SENDMESSAGES_JOB, sleepBeforeShutdown);
		DBUtils.sleep(sleepBeforeShutdown);
		
		this.log.info("Stopping H2 Database...");
		DBUtils.stopH2Database();
		
		this.log.info("Mi assicuro che il Job [{}] abbia sollevato un'eccezione del DB oppure che abbia completato.", SENDMESSAGES_JOB);
		final JobExecution brokenSendMessageExecution = brokenSendMessage.get();
		
		// Gli stati possono essere due: o il job è finito prima di aver chiuso il db, oppure è rotto perchè si è chiuso il db sotto.
		BatchStatus status = null;
		if (brokenSendMessageExecution != null) {
			this.log.info("Il Job [{}] è rimasto in stato {}", SENDMESSAGES_JOB,brokenSendMessageExecution);
			status = brokenSendMessageExecution.getStatus();
		}
		
		this.log.info("Attendo che il db si riprenda");
		DBUtils.awaitForDb(jobExplorer, SENDMESSAGES_JOB);
		return status;
	}
	
	
	
	
	
	// TODO: LA gestione delle eccezioni deve essere fatta fuori la future, o cmq solleviamo l'eccezione puntualmente e nel test guardiamo la cause	
	
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
	
	
	private Future<JobExecution> runSendMessageJobAsync() {
		return executor.submit( () -> {
			try {
				return govioBatchService.runSendMessageJob();
			} catch (CannotCreateTransactionException | TransactionSystemException e) {
				// Se il db è andato giù, restituiamo null
				return null;
			}
		});
	}
	
	
	AtomicInteger messagesReceived = new AtomicInteger(0);
	Set<String> codiciFiscali = new HashSet<>();
	List<String> codiciFiscaliDuplicati = new ArrayList<>();
	
	
	private void buildMocks() {

		Mockito.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<LimitedProfile>() {
		}))).thenAnswer(new Answer<ResponseEntity<LimitedProfile>>() {
			@Override
			public ResponseEntity<LimitedProfile> answer(InvocationOnMock invocation) throws Exception {
				// String fiscalCode = ((RequestEntity<FiscalCodePayload>) invocation.getArgument(0)).getBody().getFiscalCode();

				LimitedProfile profile = new LimitedProfile();
				profile.setSenderAllowed(true);
				return new ResponseEntity<LimitedProfile>(profile, HttpStatus.OK);
			}
		});

		Mockito.when(restTemplate.exchange(any(), eq(new ParameterizedTypeReference<CreatedMessage>() {
		}))).thenAnswer(new Answer<ResponseEntity<CreatedMessage>>() {
			@Override
			public ResponseEntity<CreatedMessage> answer(InvocationOnMock invocation) throws InterruptedException {
				@SuppressWarnings("unchecked")
				String fiscalCode = ((RequestEntity<NewMessage>) invocation.getArgument(0)).getBody().getFiscalCode();
				if (codiciFiscali.contains(fiscalCode)) {
					log.error("Trovato mesaggio duplicato, con destinatario: {}", fiscalCode);
					codiciFiscaliDuplicati.add(fiscalCode);
				}

				codiciFiscali.add(fiscalCode);
				messagesReceived.incrementAndGet();
				
				CreatedMessage createdMessage = new CreatedMessage();
				createdMessage.setId(UUID.randomUUID().toString());
				return new ResponseEntity<CreatedMessage>(createdMessage, HttpStatus.CREATED);
			}
		});
		
		Mockito.when(restTemplate.getUriTemplateHandler()).thenReturn(new DefaultUriBuilderFactory());
	}
	
}
