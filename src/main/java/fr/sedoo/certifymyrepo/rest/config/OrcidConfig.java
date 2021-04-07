package fr.sedoo.certifymyrepo.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class OrcidConfig {

	@Value("${CLIENT_ID}")
	private String clientId;
	
	@Value("${CLIENT_SECRET}")
	private String clientSecret;
	
	@Value("${orcid.tokenUrl}")
	private String tokenUrl;
	
	@Value("${orcid.publicApiUrl}")
	private String publicApiUrl;
	
}
