package fr.sedoo.certifymyrepo.rest.service.v1_0.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason="You don't have the right to do this operation")
public class ForbiddenException extends RuntimeException {}