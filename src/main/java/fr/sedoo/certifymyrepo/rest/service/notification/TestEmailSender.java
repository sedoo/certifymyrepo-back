package fr.sedoo.certifymyrepo.rest.service.notification;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.MailConfig;
import fr.sedoo.certifymyrepo.rest.config.Profiles;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;

@Component
@Profile(Profiles.DEV_PROFILE)
public class TestEmailSender implements EmailSender {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestEmailSender.class);
	
	@Autowired
	private MailConfig mailConfig;
	
	@Value("${EMAIL_NOTIFICATION_DEV}")
	private String emailDev;

	@Override
	public boolean sendNotification(ContactDto contact) {
		try {
			SimpleEmail simpleemail = new SimpleEmail();
			simpleemail.setHostName(mailConfig.getHostname());
			contact.getTo().toArray(new String[contact.getTo().size()]);
			simpleemail.addTo(emailDev);
			if(contact.getFromEmail() != null) {
				if(contact.getFromName() != null) {
					simpleemail.setFrom(contact.getFromEmail(), contact.getFromName());
				} else {
					simpleemail.setFrom(contact.getFromEmail());
				}
			} else {
				simpleemail.setFrom(mailConfig.getFrom());
			}
			simpleemail.setSubject("[Test Message] ".concat(contact.getSubject()));
			
			StringBuilder sb = new StringBuilder();
			sb.append("Former recipients:");
			for (String email : contact.getTo()) {
				sb.append(" ").append(email);
			}
			sb.append("\n\n").append(contact.getMessage());
			
			simpleemail.setMsg(sb.toString());
			simpleemail.send();
			return true;
		} catch (EmailException e) {
			LOG.error("Notification could not be send", e);
			return false;
		}
	}

}
