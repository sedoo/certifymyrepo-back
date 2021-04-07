package fr.sedoo.certifymyrepo.rest.filter.jwt;

import javax.annotation.PostConstruct;

public class JwtConfig {

	private String signingKey;
	private Integer tokenValidity;
	
		
	public JwtConfig() {
	}
	
	@PostConstruct
	public void init() {
		if (getSigningKey() == null){
			setSigningKey("secretkey");
		}
		if (getTokenValidity() == null){
			setTokenValidity(JwtUtil.DEFAULT_VALIDITY);
		}
	}

	public String getSigningKey() {
		return signingKey;
	}
	public void setSigningKey(String signingKey) {
		this.signingKey = signingKey;
	}
	
	public Integer getTokenValidity() {
		return tokenValidity;
	}
	public void setTokenValidity(Integer tokenValidity) {
		this.tokenValidity = tokenValidity;
	}
	
}