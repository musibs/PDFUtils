package io.mulabs.pdfutils.exceptions;

public class InvalidPdfException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public InvalidPdfException(String message) {
		super(message);
	}
	
	public InvalidPdfException(String message, Throwable th) {
		super(message, th);
	}
	
}
