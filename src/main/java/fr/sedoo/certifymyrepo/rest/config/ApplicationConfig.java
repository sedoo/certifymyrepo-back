package fr.sedoo.certifymyrepo.rest.config;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ApplicationConfig {
	
	@Value("${tmpFolder}/download")
	private String temporaryDownloadFolderName;
	
	@Value("${temporary.storageDuration}")
	Integer storageDuration;
	
	@PostConstruct
	public void init() {
		File aux = new File(temporaryDownloadFolderName);
		if (!aux.exists()) {
			aux.mkdirs();
		}
	}

}
