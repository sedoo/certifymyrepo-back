package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;

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
	
	@Autowired
	private ApplicationConfig appConfig;

	@Scheduled(cron = "${notification.cronExpression}")
	public void notificationUnsedRepository() {
		
		Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, -delay);
		List<CertificationReport> reportList = reportDao.findInProgressByUpdateDateLowerThan(c.getTime());
		if(reportList != null) {
			for(CertificationReport report : reportList) {
				buildNotification(report.getRepositoryId(), appConfig.getNoActivityNotificationSubject(), 
						appConfig.getRepositoryAccessFrenchContent(),
						appConfig.getNoActivityNotificationEnglishContent());
			}
		}
	}
	
	/**
	 * Build ContactDto object and send notification
	 * @param repositoryId repository identifier
	 * @param subject
	 * @param frenchContent
	 * @param englishContent
	 * @param message optional
	 */
	private void buildNotification(String repositoryId, String subject, 
			String frenchContent, String englishContent) {
		// notification the report has been validated
		Repository repo = repositoryDao.findById(repositoryId);
		// List user id in DB
		List<String> repoUsersEmail = new ArrayList<String>();
		Set<String> repoUserIdList = repo.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
		for(String userId : repoUserIdList) {
			Optional<Profile> userProfile = profileDao.findById(userId);
			if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
				repoUsersEmail.add(userProfile.get().getEmail());
			}
		}
		if( repoUsersEmail != null && repoUsersEmail.size() > 0) {
			ContactDto contact = new  ContactDto();
			Set<String> to = new HashSet<String>();
			to.addAll(repoUsersEmail);
			contact.setTo(to);
			contact.setSubject(String.format(subject, repo.getName(), repo.getName()));
			String content = appConfig.getEnglishHeader().concat("<br/><br/>");
			content = content.concat(String.format(frenchContent, repo.getName()))
						.concat("<br/><br/>").concat(String.format(englishContent, repo.getName()));
			contact.setMessage(content);

			emailSender.sendNotification(contact);
		}
	}
	


}
