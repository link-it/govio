/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
package it.govio.batch;

import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
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

	public static final String GOVIOJOBID_STRING = "GovioJobID";
	public static final String CURRENTDATE_STRING = "CurrentDate";
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void fileProcessingJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIOJOBID_STRING, String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(fileProcessingJob, params);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.sendMessageJob.fixedDelayString:60000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void sendMessageJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIOJOBID_STRING, String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(sendMessagesJob, params);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.verifyMessagesJob.fixedDelayString:600000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void verifyMessagesJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		JobParameters params = new JobParametersBuilder()
				.addString(GOVIOJOBID_STRING, String.valueOf(System.currentTimeMillis()))
				.addDate(CURRENTDATE_STRING,new Date())
				.toJobParameters();
		jobLauncher.run(verifyMessagesJob, params);
	}
}
