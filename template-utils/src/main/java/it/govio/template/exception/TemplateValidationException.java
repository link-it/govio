package it.govio.template.exception;

/**
 * Errore in invocazione del servizio di backend IO
 * @author nardi
 *
 */
public class TemplateValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public TemplateValidationException(String cause) {
		super(cause);
	}

}
