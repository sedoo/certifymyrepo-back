package fr.sedoo.certifymyrepo.rest.filter.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Setter
@Component
public class JwtConfig {

	@Value("${jwt.signingKey}")
	private String signingKey;
	
	@Value("${jwt.token.validity}")
	private String tokenValidity;
	
	@Value("${jwt.token.access.resquest.validity}")
	private String tokenAccessResquestValidity;
	
	public Integer getTokenValidity() {
		return Integer.valueOf(this.tokenValidity);
	}
	
	public Integer getTokenAccessResquestValidity() {
		return Integer.valueOf(this.tokenAccessResquestValidity);
	}

	public String getSigningKey() {
		return this.signingKey;
	}
	
}