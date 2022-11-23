package it.govio.batch.config;


import java.util.concurrent.Future;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.BackendioRuntimeException;
import it.govio.batch.step.GetMessageProcessor;

@Configuration
public class VerifyMessagesJobConfig extends AbstractMessagesJobConfig {

	@Autowired
	private GetMessageProcessor getMessageProcessor;
	
	@Bean(name = "VerifyMessagesJob")
	public Job verificaMessaggiIO(){
		return jobs.get("VerifyMessagesJob")
				.incrementer(new RunIdIncrementer())
				.start(getMessageStep())
				.build();
	}
	
	public Step getMessageStep(){
		Status[] statuses = {Status.SENT, Status.THROTTLED, Status.ACCEPTED};
		return steps.get("getMessaggeStep")
		.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
		.reader(expiredScheduledDateMessageCursor(statuses))
		.processor(asyncProcessor(this.getMessageProcessor))
		.writer(asyncMessageWriter())
		.faultTolerant()
		.skip(BackendioRuntimeException.class)
		.skipLimit(Integer.MAX_VALUE)
		.build();
	}
	
}
