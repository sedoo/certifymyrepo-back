package fr.sedoo.certifymyrepo.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.sedoo.sso.utils.jwt.PublicKeyUtils;
import lombok.Getter;

@Component
@Getter
public class SsoKeyLoader {

	@Value("${sso.authorizedUrls}")    
	String[] ssoAuthorizedUrls;

	Map<String, String> publicKeys = new HashMap<>();
	
	@PostConstruct
	public void initPublicKeys() {
		publicKeys = PublicKeyUtils.loadKeys(ssoAuthorizedUrls);
	}
	
}

