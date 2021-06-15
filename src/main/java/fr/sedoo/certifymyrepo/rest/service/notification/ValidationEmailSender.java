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
@Profile(Profiles.PRE_PRODUCTION_PROFILE)
public class ValidationEmailSender implements EmailSender {
	
	private static final Logger LOG = LoggerFactory.getLogger(ValidationEmailSender.class);
	
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
		List<String> functionalAdminEmails = new ArrayList<String>();
		List<Admin> functionalAdmins = adminDao.findAllFunctaionalAdmin();
		for(Admin functionalAdmin : functionalAdmins) {
			Optional<fr.sedoo.certifymyrepo.rest.domain.Profile> userProfile = profileDao.findById(functionalAdmin.getUserId());
			if(userProfile.isPresent()) {
				functionalAdminEmails.add(userProfile.get().getEmail());
			}
		}
		try {
			SimpleEmail simpleemail = new SimpleEmail();
			simpleemail.setHostName(mailConfig.getHostname());
			//simpleemail.addTo("thomas.romuald@obs-mip.fr");
			simpleemail.addTo(functionalAdminEmails.toArray(new String[functionalAdminEmails.size()]));
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
			simpleemail.setSubject("['Crusöe validation platform] ".concat(contact.getSubject()));
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
