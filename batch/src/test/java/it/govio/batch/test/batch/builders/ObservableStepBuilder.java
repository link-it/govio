package it.govio.batch.test.batch.builders;

import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.CompletionPolicy;

/**
 *	 Step Builder Osservabile che hooka dei listener configurati dall'applicazione, in base al nome
 *  dello step da generare.
 * 
 *  Utile in fase di test, o per il supporto a un sistema di monitoraggio e configurazione dei batch.
 *
 */
public class ObservableStepBuilder extends StepBuilder {

	public Set<StepListener> listeners = new HashSet<>();
	
	Logger log = LoggerFactory.getLogger(ObservableStepBuilder.class);
	
	public ObservableStepBuilder(String name) {
		super(name);
		log.debug("Using Custom ObservableStepBuilder.");
	}
	

	@Override
	public <I, O> SimpleStepBuilder<I, O> chunk(int chunkSize) {
		SimpleStepBuilder<I, O> ret = super.chunk(chunkSize);
		addListeners(ret);
		return ret;
	}
	
	
	@Override
	public <I, O> SimpleStepBuilder<I, O> chunk(CompletionPolicy completionPolicy) {
		SimpleStepBuilder<I, O> ret =	super.chunk(completionPolicy);
		addListeners(ret);
		return ret;
	}
	
	
	@SuppressWarnings("unchecked")	
	protected <I,O> void addListeners(SimpleStepBuilder<I,O> builder) {
		for(var l : listeners) {
			if (l instanceof ChunkListener) { 
				builder.listener((ChunkListener) l);
			}
			else if (l instanceof ItemProcessListener<?, ?>) {
				builder.listener( (ItemProcessListener<I,O>) l);
			}
			else if(l instanceof ItemReadListener<?> ) {
				builder.listener( (ItemReadListener<I>) l );
			}
			else if(l instanceof ItemWriteListener<?>) {
				builder.listener( (ItemWriteListener<O>) l);
			}
		}
	}

}
