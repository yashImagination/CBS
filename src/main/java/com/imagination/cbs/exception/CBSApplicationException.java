package com.imagination.cbs.exception;

public class CBSApplicationException extends RuntimeException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String errorMessage;

	@Override
	public String getMessage() {
		return errorMessage;
	}

	public CBSApplicationException() {
		super();
	}

	public CBSApplicationException(String errorMessage) {
		super(errorMessage);
		this.errorMessage = errorMessage;
	}
}