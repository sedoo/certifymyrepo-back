package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.MailConfig;
import fr.sedoo.certifymyrepo.rest.config.Profiles;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;

@Component
@Profile(Profiles.PRODUCTION_PROFILE)
public class ProdEmailSender implements EmailSender {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProdEmailSender.class);

	@Autowired
	private AdminDao adminDao;
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private MailConfig mailConfig;

	@Override
	public boolean sendNotification(ContactDto contact) {
		List<String> superAdminEmails = new ArrayList<String>();
		List<Admin> superAdmins = adminDao.findAllSuperAdmin();
		for(Admin superAdmin : superAdmins) {
			Optional<fr.sedoo.certifymyrepo.rest.domain.Profile> userProfile = profileDao.findById(superAdmin.getUserId());
			if(userProfile.isPresent()) {
				superAdminEmails.add(userProfile.get().getEmail());
			}
		}
		try {
			SimpleEmail simpleemail = new SimpleEmail();
			simpleemail.setHostName(mailConfig.getHostname());
			simpleemail.addTo(contact.getTo().toArray(new String[contact.getTo().size()]));
			simpleemail.addCc(superAdminEmails.toArray(new String[superAdminEmails.size()]));
			if(contact.getFromEmail() != null) {
				if(contact.getFromName() != null) {
					simpleemail.setFrom(contact.getFromEmail(), contact.getFromName());
				} else {
					simpleemail.setFrom(contact.getFromEmail());
				}
			} else {
				simpleemail.setFrom(mailConfig.getFrom());
			}
			simpleemail.setSubject(contact.getSubject());
			simpleemail.setMsg(contact.getMessage());
			simpleemail.send();
			return true;
		} catch (EmailException e) {
			LOG.error("Notification could not be send", e);
			return false;
		}
	}

}
