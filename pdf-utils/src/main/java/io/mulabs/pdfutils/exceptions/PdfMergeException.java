package io.mulabs.pdfutils.exceptions;

public class PdfMergeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PdfMergeException(String message) {
		super(message);
	}
	
	public PdfMergeException(String message, Throwable th) {
		super(message, th);
	}
	
}
