package it.govio.batch.test.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

public class ChunkLogListener implements ChunkListener{

	Logger log = LoggerFactory.getLogger(ChunkLogListener.class);

	@Override
	public void beforeChunk(ChunkContext context) {
		log.info("BEFORE CHUNK");
		
	}

	@Override
	public void afterChunk(ChunkContext context) {
		log.info("AFTER CHUNK");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterChunkError(ChunkContext context) {
		// TODO Auto-generated method stub
		
	}

}
