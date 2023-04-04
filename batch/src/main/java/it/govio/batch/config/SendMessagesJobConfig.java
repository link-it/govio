/*
 * GovIO - Notification system for AppIO
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
