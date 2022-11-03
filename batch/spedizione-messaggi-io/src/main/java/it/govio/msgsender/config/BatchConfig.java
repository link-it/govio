package it.govio.msgsender.config;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.step.GetProfileProcessor;
import it.govio.msgsender.writer.GetProfileWriter;
import it.govio.msgsender.step.GdcDBItemReader;


@Configuration
@EnableBatchProcessing
public class BatchConfig  {

	@Autowired
	private JobBuilderFactory jobs;

	@Autowired
	private StepBuilderFactory steps;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	private GetProfileProcessor getProfilePaProcessor;
	
	@Autowired
	private GdcDBItemReader itemReader;
	
	@Autowired
	private GetProfileWriter itemWriter;
	
	@Bean
	public Step msgsenderGovio(){
		return steps.get("msgsenderGovio").<GovioMessageEntity, GovioMessageEntity>chunk(1)
				.reader(this.itemReader)
				.processor(this.getProfilePaProcessor)
				.writer(this.itemWriter)
				.build();
	}

	@Bean
	public Job acquisizioneGdCJob(){
		return jobs.get("riconciliatoreRiversamentiPagoPaJob")
				.incrementer(new RunIdIncrementer())
				.start(msgsenderGovio())
				.build();
	}
}
