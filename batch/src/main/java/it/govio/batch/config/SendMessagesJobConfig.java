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
import it.govio.batch.step.GetProfileProcessor;
import it.govio.batch.step.NewMessageProcessor;

@Configuration
public class SendMessagesJobConfig extends AbstractMessagesJobConfig {

	@Autowired
	private GetProfileProcessor getProfileProcessor;

	@Autowired
	private NewMessageProcessor newMessageProcessor;
	
	@Bean(name = "SendMessagesJob")
	public Job spedizioneMessaggiIO(){
		return jobs.get("SendMessagesJob")
				.incrementer(new RunIdIncrementer())
				.start(getProfileStep())
				.next(newMessageStep())
				.build();
	}
	
	public Step getProfileStep(){
		Status[] statuses = {Status.SCHEDULED};
		return steps.get("getProfileStep")
				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
				.reader(expiredScheduledDateMessageCursor(statuses))
				.processor(asyncProcessor(this.getProfileProcessor))
				.writer(asyncMessageWriter())
				.faultTolerant()
				.skip(BackendioRuntimeException.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}
	
	public Step newMessageStep(){
		Status[] statuses = {Status.RECIPIENT_ALLOWED};
		return steps.get("newMessageStep")
				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(1)
				.reader(expiredScheduledDateMessageCursor(statuses))
				.processor(asyncProcessor(this.newMessageProcessor))
				.writer(asyncMessageWriter())
				.faultTolerant()
				.retry(BackendioRuntimeException.class)
				.retryLimit(5)
				.build();
	}
	
}
