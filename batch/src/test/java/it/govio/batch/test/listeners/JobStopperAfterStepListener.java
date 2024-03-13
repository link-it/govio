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
package it.govio.batch.test.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;

public class JobStopperAfterStepListener implements StepExecutionListener {
	
		JobOperator jobOperator;
		
		Logger log = LoggerFactory.getLogger(JobStopperAfterStepListener.class);
		
		boolean fired = false;
		
		public JobStopperAfterStepListener(JobOperator jobOperator) {
			this.jobOperator = jobOperator;
		}

		
		@Override
		public void beforeStep(StepExecution stepExecution) {	}

		
		@Override
		public ExitStatus afterStep(StepExecution stepExecution) {
			log.info("JobStopperAfterStepListener::afterStep");
			if ( ! fired ) {
				log.info("Stopping job [{}] after step [{}]", stepExecution.getJobExecution().getJobInstance().getJobName(), stepExecution.getStepName());
				try {
					this.jobOperator.stop(stepExecution.getJobExecutionId());
					fired = true;
				} catch (NoSuchJobExecutionException | JobExecutionNotRunningException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			} else {
				log.info("Job [{}] already stopped one time after step [{}]", stepExecution.getJobExecution().getJobInstance().getJobName(), stepExecution.getStepName());
			}
			
			
			return stepExecution.getExitStatus();
		}
}
