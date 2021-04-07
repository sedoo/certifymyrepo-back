package fr.sedoo.certifymyrepo.rest.service.exception;

public class BusinessException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7990549718544070461L;


	public BusinessException(String message) {
		super(message);
	}
}
