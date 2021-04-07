package fr.sedoo.certifymyrepo.rest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class FtpConfiguration {
	
	@Value("${ftp.host}")
	private String host;

	@Value("${ftp.username}")
	private String login;

	@Value("${FTP_PASSWORD}")
	private String password;
}
