package it.govio.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
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
