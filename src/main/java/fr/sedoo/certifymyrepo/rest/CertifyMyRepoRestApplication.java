package fr.sedoo.certifymyrepo.rest;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import fr.sedoo.certifymyrepo.rest.config.DefaultAdminUtils;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = {"fr.sedoo.certifymyrepo"} )
public class CertifyMyRepoRestApplication {
	
	@Autowired
	DefaultAdminUtils defaultAdminUtils;
	
	public static void main(String[] args) {
		SpringApplication.run(CertifyMyRepoRestApplication.class, args);
	}
	
	@PostConstruct
	private void initAdmin() {
		defaultAdminUtils.init();
	}
	
	
	
}


