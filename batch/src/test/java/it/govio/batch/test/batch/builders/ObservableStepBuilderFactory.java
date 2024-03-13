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
package it.govio.batch.test.batch.builders;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.transaction.PlatformTransactionManager;

public class ObservableStepBuilderFactory extends StepBuilderFactory {
	
	public HashMap<String, StepExecutionListener> executionListeners = new HashMap<>();
	
	public HashMap<String, Set<StepListener>> stepListeners = new HashMap<>();
	
	// Le riporto qui perchè private nel padre.
	JobRepository jobRepository;

	PlatformTransactionManager transactionManager;
	
	Logger log = LoggerFactory.getLogger(ObservableJobBuilderFactory.class);

	public ObservableStepBuilderFactory(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		super(jobRepository, transactionManager);
		
		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
	}

	@Override
	public StepBuilder get(String name) {
		log.debug("Using Custom ObservableStepBuilderFactory.");
		
		ObservableStepBuilder builder = (ObservableStepBuilder) 
				new ObservableStepBuilder(name).
					repository(jobRepository).
					transactionManager(	transactionManager);
		
		StepExecutionListener l = this.executionListeners.get(name);
		if (l != null) {
			builder.listener(l);
		}
		
		Set<StepListener> sl = this.stepListeners.get(name);
		if (sl != null) {
			builder.listeners = Set.copyOf(sl);
		}
		
		return builder;
	}
}
