package it.govio.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTypeResolverBuilder;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@SpringBootApplication(scanBasePackages={"it.govio.batch","it.pagopa.io.v1.api"})
@EnableScheduling
public class Application extends SpringBootServletInitializer {

	private static final String GOVIO_JOB_ID = "GovioJobID";

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("FileProcessingJob") 
	private Job fileProcessingJob;
	
	@Autowired
	@Qualifier("SendMessagesJob") 
	private Job sendMessagesJob;
	
	@Autowired
	@Qualifier("VerifyMessagesJob") 
	private Job verifyMessagesJob;
	
	private Logger log = LoggerFactory.getLogger(Application.class);
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void fileProcessingJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException   {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIO_JOB_ID, String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(fileProcessingJob, params);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.sendMessageJob.fixedDelayString:60000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void sendMessageJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException  {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIO_JOB_ID, String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(sendMessagesJob, params);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.verifyMessagesJob.fixedDelayString:600000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void verifyMessagesJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIO_JOB_ID, String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(verifyMessagesJob, params);
	}
	
	@Autowired
	private ObjectMapper objectMapper;



}
