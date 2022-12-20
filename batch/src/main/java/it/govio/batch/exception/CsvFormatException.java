package it.govio.batch.exception;

public class CsvFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	public CsvFormatException(String message, Throwable e) {
		super(message, e);
	}
	
}
