package fr.sedoo.certifymyrepo.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin
@RequestMapping(value = "/version")
public class VersionService {

	
	@Autowired
	BuildProperties buildProperties;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}

	@RequestMapping(value = "/current", method = RequestMethod.GET)
	public String currentVersion() {
		return buildProperties.getVersion();
	}
	
	


}



