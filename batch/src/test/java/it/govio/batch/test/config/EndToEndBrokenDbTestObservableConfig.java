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

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;

import it.govio.batch.config.SendMessagesJobConfig;
import it.govio.batch.test.batch.builders.ObservableJobBuilderFactory;
import it.govio.batch.test.batch.builders.ObservableStepBuilderFactory;
import it.govio.batch.test.listeners.StepDescriptorListener;

/**
 * In questa configurazione registriamo un solo listener che imposta lo stato di esecuzione del
 * primo step del SendMessagesJob
 * 
 */
@TestConfiguration
public class EndToEndBrokenDbTestObservableConfig {

	public final StepDescriptorListener getProfileDescriptor  = new StepDescriptorListener();

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
		factory.executionListeners.put(SendMessagesJobConfig.GETPROFILE_STEPNAME,	getProfileDescriptor);
		return factory;
	}
	
	
}
