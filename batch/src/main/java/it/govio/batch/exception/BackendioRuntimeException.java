package it.govio.batch.exception;

/**
 * Errore in invocazione del servizio di backend IO
 * @author nardi
 *
 */
public class BackendioRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public BackendioRuntimeException(Throwable e) {
		super(e);
	}

}
