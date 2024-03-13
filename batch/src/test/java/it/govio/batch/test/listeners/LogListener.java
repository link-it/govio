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

public class LogListener implements StepExecutionListener {
	
	Logger log = LoggerFactory.getLogger(LogListener.class);

	@Override
	public void beforeStep(StepExecution stepExecution) {
			log.info("BEFORE STEP");
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		log.info("AFTER STEP");
		
		//stepExecution.getJobExecution().getI
		return stepExecution.getExitStatus();
	}
	
}
