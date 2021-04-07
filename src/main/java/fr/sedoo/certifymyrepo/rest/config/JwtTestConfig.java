package fr.sedoo.certifymyrepo.rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtConfig;

@Configuration
public class JwtTestConfig {
	
	@Bean
	public JwtConfig getJwtConfig() {
		return new JwtConfig();
	}

}
