package it.govio.batch;

import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication(scanBasePackages={"it.govio.batch","it.pagopa.io.v1.api"})
@EnableScheduling
public class Application extends SpringBootServletInitializer {

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
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Scheduled(fixedDelay = 5, timeUnit = TimeUnit.SECONDS)
	public void fileProcessingJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(fileProcessingJob, params);
	}
	
	@Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
	public void sendMessageJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(sendMessagesJob, params);
	}
	
	@Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
	public void verifyMessagesJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(verifyMessagesJob, params);
	}
}
