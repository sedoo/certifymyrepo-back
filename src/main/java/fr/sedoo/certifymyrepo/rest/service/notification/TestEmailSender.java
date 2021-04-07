package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.Profiles;

@Component
@Profile({Profiles.DEV_PROFILE, Profiles.PRE_PRODUCTION_PROFILE})
public class TestEmailSender implements EmailSender {
	
	@Autowired
	private Environment environment;
	
	@Override
	public void send(SimpleEmail email, String content) throws AddressException, EmailException {
		List<InternetAddress> toAddresses = email.getToAddresses();
		StringBuilder sb = new StringBuilder();
		sb.append("Former recipients:");
		for (InternetAddress internetAddress : toAddresses) {
			sb.append(" "+internetAddress.getAddress());
		}
		
		List<String> profiles = Arrays.asList(environment.getActiveProfiles());
		String subject = "[Test Message] "+ email.getSubject();
		if (profiles.contains(Profiles.DEV_PROFILE)) {
			subject = "[Message from developer workstation] "+ email.getSubject();
		}
		email.setSubject(subject);
		List<InternetAddress> newToAdresses = new ArrayList<>();
		newToAdresses.add(new InternetAddress("thomas.romuald@obs-mip.fr"));
		email.setTo(newToAdresses);
		email.setMsg(sb.toString()+"\n\n"+content);
		email.send();
	}

}
