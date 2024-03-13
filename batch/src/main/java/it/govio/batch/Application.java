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
package it.govio.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import it.govio.batch.config.FileProcessingJobConfig;
import it.govio.batch.config.SendMessagesJobConfig;
import it.govio.batch.config.VerifyMessagesJobConfig;
import it.govio.batch.service.GovioBatchService;

@SpringBootApplication(scanBasePackages={"it.govio.batch","it.pagopa.io.v1.api"})
@EnableScheduling
public class Application extends SpringBootServletInitializer {

	private Logger log = LoggerFactory.getLogger(Application.class);
	
	@Autowired
	GovioBatchService govioBatches;
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void fileProcessingJob() throws  Exception  {
		this.log.info("Running scheduled {}", FileProcessingJobConfig.FILEPROCESSING_JOB);
		this.govioBatches.runFileProcessingJob();
	}
	
	@Scheduled(fixedDelayString = "${scheduler.sendMessageJob.fixedDelayString:60000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void sendMessageJob() throws Exception  {
		this.log.info("Running scheduled {}", SendMessagesJobConfig.SENDMESSAGES_JOB);
		this.govioBatches.runSendMessageJob();
	}
	
	@Scheduled(fixedDelayString = "${scheduler.verifyMessagesJob.fixedDelayString:600000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void verifyMessagesJob() throws Exception {
		this.log.info("Running scheduled {}", VerifyMessagesJobConfig.VERIFYMESSAGES_JOBNAME);
		this.govioBatches.runVerifyMessagesJob();
	}
	
}
