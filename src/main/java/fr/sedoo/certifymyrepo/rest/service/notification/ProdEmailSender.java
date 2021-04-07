package fr.sedoo.certifymyrepo.rest.service.notification;

import javax.mail.internet.AddressException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.Profiles;

@Component
@Profile(Profiles.PRODUCTION_PROFILE)
public class ProdEmailSender implements EmailSender {

	
	@Override
	public void send(SimpleEmail email, String content) throws AddressException, EmailException {

		email.setMsg(content);
		email.send();
	}

}
