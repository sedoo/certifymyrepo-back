package fr.sedoo.certifymyrepo.rest.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserNotLoggedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
}
