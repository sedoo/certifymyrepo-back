package fr.sedoo.certifymyrepo.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class OrcidConfig {

	@Value("${orcid.clientId}")
	private String clientId;
	
	@Value("${orcid.clientSecret}")
	private String clientSecret;
	
	@Value("${orcid.tokenUrl}")
	private String tokenUrl;
	
	@Value("${orcid.publicApiUrl}")
	private String publicApiUrl;
	
}
