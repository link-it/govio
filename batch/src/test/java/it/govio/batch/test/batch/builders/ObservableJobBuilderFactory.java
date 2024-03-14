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

import org.slf4j.LoggerFactory;

import java.util.HashMap;

import org.slf4j.Logger;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;

/**
 *	 Factory per JobBuilder che hooka dei listener configurati dall'applicazione, in base al nome
 *  dello step da generare.
 * 
 *  Utile in fase di test, o per il supporto a un sistema di monitoraggio e configurazione dei batch.
 *
 */
public class ObservableJobBuilderFactory extends JobBuilderFactory {
	
	public HashMap<String, JobExecutionListener> listeners = new HashMap<>();
	
	Logger log = LoggerFactory.getLogger(ObservableJobBuilderFactory.class);

	public ObservableJobBuilderFactory(JobRepository jobRepository) {
		super(jobRepository);
	}


	@Override
	public JobBuilder get(String name) {
		log.debug("Using custom ObservableJobBuilderFactory.");
		JobBuilder ret = super.get(name);
		
		JobExecutionListener l = listeners.get(name);
		if (l != null) {
			ret.listener(l);
		}
		
		return ret;
	}
	
}
