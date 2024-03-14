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
package it.govio.batch.config;

import org.springframework.context.annotation.Configuration;

/**
 * Per ogni file in stato PROCESSED, elabora le statistiche sugli stati dei messaggi.
 * 
 * Bisogna evitare di rieseguire le statistiche per files che hanno completato il loro ciclo.
 * Il ciclo è completato quando nessuno stato dei messaggi può cambiare e quindi dipende 
 * dalla logica dei jobs che inviano i messaggi a partire dai files.
 * 
 * Implementare magari uno stato "CLOSED" per un file, ad indicare che tutto è stato fatto e
 * nessun job\statistica deve ripartire su di esso
 *
 */

@Configuration
public class FileStatsJobConfig {
		
	/*@Autowired
	protected StepBuilderFactory steps;
	
	@Bean(name = "FileProcessingJob")
	public Job fileProcessingJob(
			JobBuilderFactory jobs,
			@Qualifier("govioFileReaderMasterStep") Step govioFileReaderMasterStep
			){
		return jobs.get("FileProcessingJob")
				.incrementer(new RunIdIncrementer())
				.start(govioFileReaderMasterStep)
				.build();
	}
	
	
	@Bean
	@Qualifier("govioFileReaderMasterStep")
	public Step govioFileReaderMasterStep(
			Partitioner govioFilePartitioner,
			@Qualifier("loadCsvFileToDbStep") Step loadCsvFileToDbStep
			) {
		return steps.get("govioFileReaderMasterStep")
				.partitioner("loadCsvFileToDbStep", govioFilePartitioner)
				.step(loadCsvFileToDbStep)
				.taskExecutor(taskExecutor())
				.build();
	}*/

}
