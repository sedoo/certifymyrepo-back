package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;

@Component
public class NotificationUtils {
	
	@Value("${notification.unused.report.months.delay}")
	private Integer delay;
	
	@Autowired
	CertificationReportDao reportDao;
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	EmailSender emailSender;

	@Scheduled(cron = "${notification.cronExpression}")
	public void notificationUnsedRepository() {
		
		Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, -delay);
		List<CertificationReport> reportList = reportDao.findInProgressByUpdateDateLowerThan(c.getTime());
		if(reportList != null) {
			for(CertificationReport report : reportList) {
				//buildNotification(report.getRepositoryId(), "", "");
			}
		}
	}
	
//	/**
//	 * Build ContactDto object and send notification
//	 * @param repositoryId repository identifier
//	 * @param messages i18n bundle
//	 * @param key i18n key prefix ("report.validation" or "report.new.version")
//	 */
//	private void buildNotification(String repositoryId, String key, String message) {
//		// notification the report has been validated
//		Repository repo = repositoryDao.findById(repositoryId);
//		// List user id in DB
//		List<String> repoUsersEmail = new ArrayList<String>();
//		Set<String> repoUserIdList = repo.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
//		for(String userId : repoUserIdList) {
//			Optional<Profile> userProfile = profileDao.findById(userId);
//			if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
//				repoUsersEmail.add(userProfile.get().getEmail());
//			}
//		}
//		if( repoUsersEmail != null && repoUsersEmail.size() > 0) {
//			ContactDto contact = new  ContactDto();
//			Set<String> to = new HashSet<String>();
//			to.addAll(repoUsersEmail);
//			contact.setTo(to);
//			contact.setSubject(String.format(messages.getString(key.concat(".notification.subject")), repo.getName()));
//			if(message != null) {
//				contact.setMessage(String.format(messages.getString(key.concat(".notification.content")), repo.getName(), message));	
//			} else {
//				contact.setMessage(String.format(messages.getString(key.concat(".notification.content")), repo.getName()));
//			}
//
//			emailSender.sendNotification(contact);
//		}
//	}

}
