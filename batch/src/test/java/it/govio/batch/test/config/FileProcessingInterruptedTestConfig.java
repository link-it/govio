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
package it.govio.batch.test.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import it.govio.batch.test.batch.builders.ObservableJobBuilderFactory;
import it.govio.batch.test.batch.builders.ObservableStepBuilderFactory;
import it.govio.batch.test.listeners.JobStopperAfterStepListener;

/**
 * In questa configurazione facciamo in modo di interrompere l'esecuzione del
 * job, dopo il primo step di lettura e aggiornamento stato dei file "
 *
 */
@TestConfiguration
public class FileProcessingInterruptedTestConfig {

	@Autowired
	JobRegistry jobRegistry;

	@Autowired
	JobOperator jobOperator;

	@Bean
	@Primary
	public JobBuilderFactory jobBuilderFactory(JobRepository repository) {
		var factory = new ObservableJobBuilderFactory(repository);
		return factory;
	}

	
	@Bean
	@Primary
	public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository,	PlatformTransactionManager transactionManager) {
		var factory = new ObservableStepBuilderFactory(jobRepository, transactionManager);
		factory.executionListeners.put("promoteProcessingFileTasklet",
				new JobStopperAfterStepListener(this.jobOperator));
		return factory;
	}
	
}
