package fr.sedoo.certifymyrepo.rest.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.Profiles;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;

@Component
@Profile(Profiles.DEV_PROFILE)
public class TestEmailSender implements EmailSender {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestEmailSender.class);

	@Override
	public boolean sendNotification(ContactDto contact) {
		LOG.info("[Test Message] ".concat(contact.getSubject()));
		StringBuilder sb = new StringBuilder();
		sb.append("Former recipients:");
		for (String email : contact.getTo()) {
			sb.append(" ").append(email);
		}
		sb.append("\n\n").append(contact.getMessage());
		LOG.info(sb.toString());
		return true;
	}

}
