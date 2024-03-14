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
package it.govio.batch.step;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.transaction.TransactionException;

import it.govio.batch.config.SendMessagesJobConfig;

/**
 * In caso di errori di database, mette il chunk corrente dei messaggi nella coda dei 
 *  messaggi da non rispedire.
 *
 */
public class BackupMessageChunkSkipper implements SkipPolicy {
	
	Logger log = LoggerFactory.getLogger(BackupMessageChunkSkipper.class);

	@Override
	public boolean shouldSkip(Throwable t, int skipCount) throws SkipLimitExceededException {
		this.log.warn("Errore durante il newMessageStep: {}", t);
		if (t instanceof TransactionException) {
			this.log.info("Rilevata Eccezione del DB. Salvo il chunk corrente di messaggi.");
			SendMessagesJobConfig.temporaryMessageStore.putAll(SendMessagesJobConfig.temporaryChunkMessageStore);
			SendMessagesJobConfig.temporaryChunkMessageStore = new Hashtable<>();
			return true;
		}
		return false;
	}

}
