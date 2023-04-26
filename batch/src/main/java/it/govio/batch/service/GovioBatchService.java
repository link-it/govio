package it.govio.batch.service;

import java.util.Date;
import java.util.Hashtable;

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
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
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
	JobOperator jobOperator;

	@Autowired
	@Qualifier(FileProcessingJobConfig.FILEPROCESSING_JOB)
	private Job fileProcessingJob;

	@Autowired
	@Qualifier(SendMessagesJobConfig.SENDMESSAGES_JOB)
	private Job sendMessagesJob;

	@Autowired
	@Qualifier(VerifyMessagesJobConfig.VERIFYMESSAGES_JOBNAME)
	private Job verifyMessagesJob;
	
	private Logger log = LoggerFactory.getLogger(GovioBatchService.class);
	
	private static final String CURRENTDATE_STRING = "CurrentDate";

	/**
	 * Possiamo avere un'unica istanza attiva di questo Job. 
	 * Se c'è già un job passato che non è terminato ma e fermo, va ripresa l'esecuzione di quel job finchè non termina.
	 * @return 
	 * @throws JobParametersInvalidException 
	 * @throws JobInstanceAlreadyCompleteException 
	 * @throws JobRestartException 
	 * @throws JobExecutionAlreadyRunningException 
	 * @throws NoSuchJobException 
	 * @throws NoSuchJobExecutionException 
	 *  
	 */
	public JobExecution runFileProcessingJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobExecutionException, NoSuchJobException {
		
		JobInstance lastInstance = this.jobExplorer.getLastJobInstance(FileProcessingJobConfig.FILEPROCESSING_JOB);
		
		// Determino i JobParameters con cui lanciare il Job. In base al loro valore avverrà un avvio nuovo, un restart, o nulla.
		JobParameters params = null;
		JobExecution lastExecution = null;
		
		if (lastInstance != null) {
			lastExecution = this.jobExplorer.getLastJobExecution(lastInstance);
		}
		
		if (lastInstance != null && lastExecution == null) {
			log.error("Trovata istanza preesistente per il job [{}] ma senza una JobExecution associata, forse l'esecuzione deve ancora partire. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", FileProcessingJobConfig.FILEPROCESSING_JOB);
			return null;
		}
		else if (lastExecution != null) {
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
			case ABANDONED:
				log.warn("Trovata Job Execution di id {} abbandonata!", lastExecution.getId());
			case COMPLETED:
			
				// I Job Abandoned non possono essere riavviati. (Sono abbandonati appunto)
				// https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#aborting-a-job
				// Se è in stato abandoned allora assumiamo che sia stata una scelta del programmatore o di un operatore del batch metterlo in quello stato.
				// Siamo liberi di andare avanti e di eseguire un nuovo job.
				log.info("Trovata istanza preesistente per il Job [{}]. Avvio nuovo Job. ", lastExecution); //FileProcessingJobConfig.FILEPROCESSING_JOB, exitStatus, lastExecution.getStatus());
				params = new JobParametersBuilder()
						.addString("When", String.valueOf(System.currentTimeMillis()))
						.addString(GOVIO_JOB_ID, FileProcessingJobConfig.FILEPROCESSING_JOB).toJobParameters();
				return jobLauncher.run(fileProcessingJob, params);
			
			// In questo caso riavvio.
			case FAILED:
			case STOPPED:
				log.info("Trovata istanza preesistente per il Job [{}]. Riavvio il Job. ", lastExecution); //FileProcessingJobConfig.FILEPROCESSING_JOB, exitStatus, lastExecution.getStatus());
				Long newExecutionId = jobOperator.restart(lastExecution.getId());
				return jobExplorer.getJobExecution(newExecutionId);
			default:
				// STARTED, STARTING, STOPPING, UNKNOWN:
				// STARTED STARTING e STOPPING non dovremmo mai trovarli, per via del comportamento dello scheduler.
				
				// UNKNOWN - Questo possiamo scoprirlo solo operativamente.
				log.info("Trovata istanza preesistente per il Job [{}]. STATO INASPETTATO. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", lastExecution); //FileProcessingJobConfig.FILEPROCESSING_JOB, exitStatus, lastExecution.getStatus());
				return null;
			}
		}	else {
			params = new JobParametersBuilder()
					.addString("When", String.valueOf(System.currentTimeMillis()))
					.addString(GOVIO_JOB_ID, FileProcessingJobConfig.FILEPROCESSING_JOB).toJobParameters();
			return jobLauncher.run(fileProcessingJob, params);
	}
}

	public JobExecution runSendMessageJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		
		this.log.info("Copio un eventuale chunk di messaggi perso.");
		SendMessagesJobConfig.temporaryMessageStore.putAll(SendMessagesJobConfig.temporaryChunkMessageStore);
		// TODO: Aggiungi uno step finale che pulisce la map.
		
		JobParameters params = new JobParametersBuilder().
				addString(GOVIO_JOB_ID, SendMessagesJobConfig.SENDMESSAGES_JOB).
				addString("When", String.valueOf(System.currentTimeMillis())).
				toJobParameters();
		
		return jobLauncher.run(sendMessagesJob, params);
	}

	public void runVerifyMessagesJob() throws JobExecutionAlreadyRunningException, JobRestartException, 	JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIO_JOB_ID,VerifyMessagesJobConfig.VERIFYMESSAGES_JOBNAME)
				.addString("When",  String.valueOf(System.currentTimeMillis()))
				.addDate(CURRENTDATE_STRING, new Date())
				.toJobParameters();
		jobLauncher.run(verifyMessagesJob, params);
	}

}
