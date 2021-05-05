package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.Profiles;

@Component
@Profile(Profiles.PRE_PRODUCTION_PROFILE)
public class ValidationEmailSender implements EmailSender {
	
	@Override
	public void send(SimpleEmail email, String content) throws AddressException, EmailException {
		List<InternetAddress> toAddresses = email.getToAddresses();
		StringBuilder sb = new StringBuilder();
		sb.append("This email was intended to:");
		for (InternetAddress internetAddress : toAddresses) {
			sb.append(" "+internetAddress.getAddress());
		}
		
		String subject = "[Validation platform] "+ email.getSubject();
		email.setSubject(subject);
		List<InternetAddress> newToAdresses = new ArrayList<>();
		newToAdresses.add(new InternetAddress("francoise.genova@astro.unistra.fr"));
		newToAdresses.add(new InternetAddress("Gilles.OHANESSIAN@cnrs.fr"));
		newToAdresses.add(new InternetAddress("olivier.rouchon@cines.fr"));
		newToAdresses.add(new InternetAddress("seilerj@igbmc.fr"));
		email.setTo(newToAdresses);
		email.setMsg(sb.toString()+"\n\n"+content);
		email.send();
	}

}
