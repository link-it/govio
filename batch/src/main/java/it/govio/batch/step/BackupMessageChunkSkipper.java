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
