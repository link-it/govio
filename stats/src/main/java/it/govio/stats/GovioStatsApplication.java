package it.govio.stats;

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


@SpringBootApplication //(scanBasePackages={"it.govio.batch","it.pagopa.io.v1.api"})
@EnableScheduling
public class GovioStatsApplication extends SpringBootServletInitializer {

	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier("FileStatsAnalyzer") 
	private Job fileStatsAnalyzer;
		
	
	public static void main(String[] args) {
		SpringApplication.run(GovioStatsApplication.class, args);
	}

	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void fileProcessingJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(fileStatsAnalyzer, params);
	}
	

}
