package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.MailConfig;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;

@Component
public class NotificationService {
	
	private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

	@Autowired
	RepositoryDao repositoryDao;
	
	@Autowired
	CertificationReportDao certificationReportDao;
	
	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	AdminDao adminDao;
	
	@Autowired
	private MailConfig mailConfig;
	
	/**
	 * Send Notification
	 * @param usedIdToNotify
	 * @param repoName
	 * @param key add or remove
	 * @param messages
	 * @param users new users list (must be empty for remove notification)
	 */
	public boolean sendNotification(Set<String> usedIdToNotify, String repoName, String key, ResourceBundle messages, List<RepositoryUser> users) {
		List<String> superAdminEmails = new ArrayList<String>();
		List<Admin> superAdmins = adminDao.findAllSuperAdmin();
		for(Admin superAdmin : superAdmins) {
			Optional<Profile> userProfile = profileDao.findById(superAdmin.getUserId());
			if(userProfile.isPresent()) {
				superAdminEmails.add(userProfile.get().getEmail());
			}

		}
		
		for (String userId : usedIdToNotify) {
			Optional<Profile> userProfile = profileDao.findById(userId);
			if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
				try {
					SimpleEmail simpleemail = new SimpleEmail();
					simpleemail.setHostName(mailConfig.getHostname());
					simpleemail.addTo(userProfile.get().getEmail());
					simpleemail.addCc(superAdminEmails.toArray(new String[superAdminEmails.size()]));
					simpleemail.setFrom(mailConfig.getFrom());
					simpleemail.setSubject(String.format(messages.getString(key.concat(".user.notification.subject")), 
							repoName));
					String content = null;
					if(users != null && users.size() > 0) {
						content = String.format(messages.getString(key.concat(".user.notification.content")), 
								repoName, getUserRole(users, userId));
					} else {
						content = String.format(messages.getString(key.concat(".user.notification.content")), 
								repoName);
					}
					emailSender.send(simpleemail, content);
				} catch (AddressException | EmailException e) {
					LOG.error("Notification could not be send to ".concat(userProfile.get().getEmail()), e);
					return false;
				}
			} else {
				LOG.warn("Notification could not be send to ".concat(userId).concat(" this user does not have an email address"));
			}

		}
		return true;
	}
	
	/**
	 * 
	 * @param users
	 * @param userId
	 * @return Role
	 */
	private String getUserRole(List<RepositoryUser> users, String userId) {
		String role = null;
		for(RepositoryUser user : users) {
			if(StringUtils.equals(user.getId(), userId)) {
				role = user.getRole();
			}
		}
		return role;
	}

}
