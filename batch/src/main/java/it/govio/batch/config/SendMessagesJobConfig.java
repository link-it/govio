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


import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.BackendioRuntimeException;
import it.govio.batch.step.BackupSendMessageWriter;
import it.govio.batch.step.GetProfileProcessor;
import it.govio.batch.step.NewMessageProcessor;

@Configuration
public class SendMessagesJobConfig extends AbstractMessagesJobConfig {
	
	public static final String SENDMESSAGES_JOB = "SendMessagesJob";
	public static final String GETPROFILE_STEPNAME = "getProfileStep";
	public static final String NEWMESSAGE_STEPNAME = "newMessageStep"; 
	
	public static Hashtable<Long, GovioMessageEntity> temporaryMessageStore = new Hashtable<>();
	
	public static Hashtable<Long, GovioMessageEntity> temporaryChunkMessageStore = new Hashtable<>();
	
	@Value("${jobs.SendMessagesJob.steps.newMessageStep.chunk-size:10}")
	private Integer newMessageStepChunkSize;
	
	@Value("${jobs.SendMessagesJob.steps.getProfileStep.chunk-size:10}")
	private Integer getProfileStepChunkSize;

	@Autowired
	private GetProfileProcessor getProfileProcessor;

	@Autowired
	private NewMessageProcessor newMessageProcessor;
	
	@Bean(name = SENDMESSAGES_JOB)
	public Job spedizioneMessaggiIO(){

		return jobs.get(SENDMESSAGES_JOB)
				.start(getProfileStep())
				.next(newMessageStep())
				.build();
	}
	
	public Step getProfileStep(){
		Status[] statuses = {Status.SCHEDULED};
		return steps.get(GETPROFILE_STEPNAME)
				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(getProfileStepChunkSize)
				.reader(expiredScheduledDateMessageCursor(statuses))
				.processor(asyncProcessor(this.getProfileProcessor))
				.writer(asyncMessageWriter())
				.faultTolerant()
				.skip(BackendioRuntimeException.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}
	
	public Step newMessageStep(){
		AsyncItemWriter<GovioMessageEntity> writer = new AsyncItemWriter<>();
	    writer.setDelegate(backupSendMessageWriter());
	    
		Status[] statuses = {Status.RECIPIENT_ALLOWED};
		
		return steps.get(NEWMESSAGE_STEPNAME)
				.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(newMessageStepChunkSize)
				.reader(expiredScheduledDateMessageCursor(statuses))
				.processor(asyncProcessor(this.newMessageProcessor))
				.writer(writer)
				.faultTolerant()
				.retry(BackendioRuntimeException.class)
				.retryLimit(5)
				.build();
	}
	
	protected BackupSendMessageWriter backupSendMessageWriter() {
		BackupSendMessageWriter writer = new BackupSendMessageWriter();
		writer.setMessageRepo(govioMessagesRepository);
		return writer;
	}
	
	protected ItemReader<GovioMessageEntity> expiredScheduledDateMessageCursor(Status[] statuses) {
		
		final String query = "SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE msg.status IN :statuses AND msg.scheduledExpeditionDate < CURRENT_TIMESTAMP";
		
        JpaCursorItemReader<GovioMessageEntity> itemReader = new JpaCursorItemReader<>();
        itemReader.setQueryString(query);
        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        itemReader.setSaveState(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statuses", Arrays.asList(statuses));
        itemReader.setParameterValues(parameters);
        return itemReader;
    }
	
	
}
