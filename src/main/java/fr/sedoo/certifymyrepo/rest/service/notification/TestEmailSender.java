package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
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
@Profile(Profiles.DEV_PROFILE)
public class TestEmailSender implements EmailSender {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestEmailSender.class);
	
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
		LOG.info("{} admins has been found", superAdmins.size());
		for(Admin superAdmin : superAdmins) {
			Optional<fr.sedoo.certifymyrepo.rest.domain.Profile> userProfile = profileDao.findById(superAdmin.getUserId());
			LOG.info(" admin id {}", superAdmin.getUserId());
			if(userProfile.isPresent()) {
				superAdminEmails.add(userProfile.get().getEmail());
				LOG.info(userProfile.get().getEmail());
			}
		}
		try {
			HtmlEmail email = new HtmlEmail();
			LOG.info(mailConfig.getHostname());
			email.setHostName(mailConfig.getHostname());
			email.addTo(superAdminEmails.toArray(new String[superAdminEmails.size()]));
			if(contact.getFromEmail() != null) {
				if(contact.getFromName() != null) {
					email.setFrom(contact.getFromEmail(), contact.getFromName());
				} else {
					email.setFrom(contact.getFromEmail());
				}
			} else {
				email.setFrom(mailConfig.getFrom());
			}
			if(StringUtils.isNotEmpty(contact.getCategory())) {
				email.setSubject("[Crusöe] [".concat(contact.getCategory()).concat("] ").concat(contact.getSubject()));
			} else {
				email.setSubject("[Crusöe] ".concat(contact.getSubject()));
			}
			StringBuilder msg = new StringBuilder();
			msg.append("<html><body>");
			msg.append(contact.getMessage());
			msg.append("</body></html>");
			email.setHtmlMsg(msg.toString());
			email.send();
			return true;
		} catch (EmailException e) {
			LOG.error("Notification could not be send", e);
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

}
