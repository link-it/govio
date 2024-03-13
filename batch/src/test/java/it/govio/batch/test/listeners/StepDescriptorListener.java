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

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class StepDescriptorListener implements StepExecutionListener {
	
	private boolean stepStarted = false;
	
	public boolean isStepStarted() {
		return stepStarted;
	}

	public boolean isStepEnded() {
		return stepEnded;
	}

	private boolean stepEnded = false;

	@Override
	public void beforeStep(StepExecution stepExecution) {
		stepStarted = true;
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
			stepEnded = true;
		}
		return stepExecution.getExitStatus();
	}

}
