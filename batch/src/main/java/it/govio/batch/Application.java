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
//@Import(CommonsExportedBeans.class)
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

	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void fileProcessingJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(fileProcessingJob, params);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.sendMessageJob.fixedDelayString:60000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void sendMessageJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(sendMessagesJob, params);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.verifyMessagesJob.fixedDelayString:600000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void verifyMessagesJob() throws Exception {
		JobParameters params = new JobParametersBuilder()
				.addString("GovioJobID", String.valueOf(System.currentTimeMillis()))
				.toJobParameters();
		jobLauncher.run(verifyMessagesJob, params);
	}
}
