package fr.sedoo.certifymyrepo.rest.filter.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class JwtConfig {

	@Value("${SIGNIN_KEY}")
	private String signingKey;
	
	@Value("${TOKEN_VALIDITY}")
	private Integer tokenValidity;
	
	@Value("${TOKEN_ACCESS_REQUEST_VALIDITY}")
	private Integer tokenAccessResquestValidity;
	
}