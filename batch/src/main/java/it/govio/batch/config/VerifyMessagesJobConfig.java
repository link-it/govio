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


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.govio.batch.entity.GovioMessageEntity;
import it.govio.batch.entity.GovioMessageEntity.Status;
import it.govio.batch.exception.BackendioRuntimeException;
import it.govio.batch.step.GetMessageProcessor;

@Configuration
public class VerifyMessagesJobConfig extends AbstractMessagesJobConfig {

	public static final String VERIFYMESSAGES_JOBNAME = "VerifyMessagesJob";

	@Value( "${govio.batch.verify-messages.delay-mins:30}" )
	protected int delay;
	
	@Value( "${govio.batch.verify-messages.window-days:14}" )
	protected int window;	
	
	@Bean(name = VERIFYMESSAGES_JOBNAME)
	public Job verificaMessaggiIO(@Qualifier("getMessageStep") Step getMessageStep){
		return jobs.get(VERIFYMESSAGES_JOBNAME)
				.incrementer(new AddParametersIncrementer())
				.start(getMessageStep)
				.build();
	}
	
	@Bean("getMessageStep")
	public Step getMessageStep(@Qualifier("expiredExpeditionDateMessageCursor") ItemReader<GovioMessageEntity> expiredExpeditionDateMessageCursor, GetMessageProcessor getMessageProcessor){
		return steps.get("getMessaggeStep")
		.<GovioMessageEntity, Future<GovioMessageEntity>>chunk(10)
		.reader(expiredExpeditionDateMessageCursor)
		.processor(asyncProcessor(getMessageProcessor))
		.writer(asyncMessageWriter())
		.faultTolerant()
		.skip(BackendioRuntimeException.class)
		.skipLimit(Integer.MAX_VALUE)
		.build();
	}
	
	@Bean("expiredExpeditionDateMessageCursor")
	@StepScope
	protected JpaCursorItemReader<GovioMessageEntity> expiredExpeditionDateMessageCursor(@Value("#{jobParameters[CurrentDate]}") Date now) {
		OffsetDateTime from = now.toInstant().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime().minusMinutes(delay);
		OffsetDateTime to = now.toInstant().atZone(ZoneId.of("Europe/Rome")).toOffsetDateTime().minusDays(window);
		Status[] statuses = {Status.SENT, Status.THROTTLED, Status.ACCEPTED};
        JpaCursorItemReader<GovioMessageEntity> itemReader = new JpaCursorItemReader<>();
        itemReader.setQueryString("SELECT msg FROM GovioMessageEntity msg JOIN FETCH msg.govioServiceInstance srv WHERE msg.status IN :statuses AND msg.expeditionDate < :t0 AND msg.expeditionDate > :t1");
        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        itemReader.setSaveState(true);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("statuses", Arrays.asList(statuses));
        parameters.put("t0", from);        
        parameters.put("t1", to);
        itemReader.setParameterValues(parameters);
        return itemReader;
    }
	
}
