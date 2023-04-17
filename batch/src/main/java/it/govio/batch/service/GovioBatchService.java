package it.govio.batch.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import it.govio.batch.config.FileProcessingJobConfig;
import it.govio.batch.config.SendMessagesJobConfig;
import it.govio.batch.config.VerifyMessagesJobConfig;

@Service
public class GovioBatchService {

	private static final String GOVIO_JOB_ID = "GovioJobID";

	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobExplorer jobExplorer;

	@Autowired
	@Qualifier(FileProcessingJobConfig.FILEPROCESSING_JOBNAME)
	private Job fileProcessingJob;

	@Autowired
	@Qualifier(SendMessagesJobConfig.SENDMESSAGES_JOBNAME)
	private Job sendMessagesJob;

	@Autowired
	@Qualifier(VerifyMessagesJobConfig.VERIFYMESSAGES_JOBNAME)
	private Job verifyMessagesJob;
	
	private Logger log = LoggerFactory.getLogger(GovioBatchService.class);

	/**
	 * Possiamo avere un'unica istanza attiva di questo Job. 
	 * Se c'è già un job passato che non è terminato ma e fermo, va ripresa l'esecuzione di quel job finchè non termina.
	 * @return 
	 *  
	 */
	public JobExecution runFileProcessingJob() throws JobExecutionAlreadyRunningException, JobRestartException,	JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		
		JobInstance lastInstance = this.jobExplorer.getLastJobInstance(FileProcessingJobConfig.FILEPROCESSING_JOBNAME);
		
		// Determino i JobParameters con cui lanciare il Job. In base al loro valore avverrà un avvio nuovo, un restart, o nulla.
		JobParameters params = null;
		
		if (lastInstance != null) {
			JobExecution lastExecution = this.jobExplorer.getLastJobExecution(lastInstance);
			
			if (lastExecution != null) {
				ExitStatus exitStatus = lastExecution.getExitStatus();
				
				// L'Exit Status di un Job è così determinato:
				// 			- 	If the Step ends with ExitStatus of FAILED, the BatchStatus and ExitStatus of the Job are both FAILED.
				// 			-	Otherwise, the BatchStatus and ExitStatus of the Job are both COMPLETED.
				//		https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#batchStatusVsExitStatus
				//
				// In questo caso batchStatus e exitStatus combaciano perchè non c'è nessuna logica particolare nel FileProcessingJobConfig
				// che altera lo stato del job nel caso gli step falliscano.
				switch (lastExecution.getStatus()) {

				// In questo caso Creo un nuovo Job.
				case COMPLETED:
				case ABANDONED:
					// I Job Abandoned non possono essere riavviati. (Sono abbandonati appunto)
					// https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#aborting-a-job
					// Se è in stato abandoned allora assumiamo che sia stata una scelta del programmatore o di un operatore del batch metterlo in quello stato.
					// Siamo liberi di andare avanti e di eseguire un nuovo job.
					log.info("Trovata istanza preesistente per il Job [{}] [ExitStatus = {}] [BatchStatus = {}], avvio nuovo Job.", FileProcessingJobConfig.FILEPROCESSING_JOBNAME, exitStatus, lastExecution.getStatus());
					params = new JobParametersBuilder()
							.addString("When", String.valueOf(System.currentTimeMillis()))
							.addString(GOVIO_JOB_ID, FileProcessingJobConfig.FILEPROCESSING_JOBNAME).toJobParameters();
					break;
				
				// In questo caso riavvio.
				case FAILED:
				case STOPPED:
					log.info("Trovata istanza preesistente per il Job [{}] [ExitStatus = {}] [BatchStatus = {}], riavvio il job.", FileProcessingJobConfig.FILEPROCESSING_JOBNAME, exitStatus, lastExecution.getStatus());
					params = lastExecution.getJobParameters();
					break;
				default:
					//STARTED, STARTING, STOPPING, UNKNOWN:
					// STARTED STARTING e STOPPING non dovremmo mai trovarli, per via del comportamento dello scheduler.
					// UNKNOWN - Questo possiamo scoprirlo solo operativamente.
					// TODO: STARTED potrebbe esserci anche nel caso in cui il db sia andato giù bruscamente.
					log.error("Trovata istanza preesistente per il Job [{}] [ExitStatus = {}] [BatchStatus = {}], STATO INASPETTATO. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", FileProcessingJobConfig.FILEPROCESSING_JOBNAME, exitStatus, lastExecution.getStatus());
					params = null;
					break;
				}
			} else { // lastExecution == null
				log.error("Trovata istanza preesistente per il job [{}] ma senza una JobExecution associata, forse l'esecuzione deve ancora partire. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", FileProcessingJobConfig.FILEPROCESSING_JOBNAME);
				params = null;
			}
		} else {
			params = new JobParametersBuilder()
					.addString("When", String.valueOf(System.currentTimeMillis()))
					.addString(GOVIO_JOB_ID, FileProcessingJobConfig.FILEPROCESSING_JOBNAME).toJobParameters();
		}
		
		if (params != null) {
			return jobLauncher.run(fileProcessingJob, params);
		} else {
			return null;
		}
		
	}

	public void runSendMessageJob() throws JobExecutionAlreadyRunningException, JobRestartException, 	JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		
		// TODO: Anche qui controllerei la presenza di una vecchia esecuzione. Ma facciamo prima dei test con dei mock che contano quanti messaggi vengono inviati.
		
		// 
		JobParameters params = new JobParametersBuilder().
				addString(GOVIO_JOB_ID, SendMessagesJobConfig.SENDMESSAGES_JOBNAME).
				addString("When", String.valueOf(System.currentTimeMillis())).
				toJobParameters();
		
		jobLauncher.run(sendMessagesJob, params);
	}

	public void runVerifyMessagesJob() throws JobExecutionAlreadyRunningException, JobRestartException, 	JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIO_JOB_ID,VerifyMessagesJobConfig.VERIFYMESSAGES_JOBNAME)
				.addString("When",  String.valueOf(System.currentTimeMillis()))
				.addDate("CurrentDate", new Date())
				.toJobParameters();
		jobLauncher.run(verifyMessagesJob, params);
	}

}
