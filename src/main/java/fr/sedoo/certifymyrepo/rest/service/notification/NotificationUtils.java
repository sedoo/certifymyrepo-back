package fr.sedoo.certifymyrepo.rest.service.notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	Logger logger = LoggerFactory.getLogger(NotificationUtils.class);
	
	@Value("${notification.unused.report.months.delay}")
	private Integer delay;
	
	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		this.delay = delay;
	}

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
	
	@Value("${notification.enabled:true}")
	private boolean isEnabled;

	@Scheduled(cron = "${notification.cronExpression}")
	public void notificationUnsedRepository() {
		if(isEnabled) {
			Calendar c = Calendar.getInstance();
	        c.setTime(new Date());
	        c.add(Calendar.MONTH, -this.getDelay());
			List<CertificationReport> reportList = reportDao.findInProgressByUpdateDateLowerThan(c.getTime());
			if(reportList != null) {
				logger.info("{} notifications has to be sent", reportList.size());
				for(CertificationReport report : reportList) {
					String reportName = report.getTemplateId();
					String formatUpdateDate = new SimpleDateFormat("yyyyMMdd").format(report.getUpdateDate());
					reportName = reportName.concat(formatUpdateDate)
							.concat("_v").concat(report.getVersion());
					
					logger.info("A notification will be send for the report {} in the repository id {}", reportName, report.getRepositoryId());
					
					buildReminderNotification(reportName, report.getRepositoryId(), appConfig.getNoActivityNotificationSubject(), 
							appConfig.getNoActivityNotificationFrenchContent(),
							appConfig.getNoActivityNotificationEnglishContent());
					report.setLastNotificationDate(new Date());
					reportDao.save(report);
				}
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
	private void buildReminderNotification(String reportName, String repositoryId, String subject, 
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
			contact.setSubject(String.format(subject, repo.getName(), this.getDelay(), repo.getName(), this.getDelay()));
			String content = appConfig.getEnglishHeader().concat("<br/><br/>");
			content = content.concat(String.format(frenchContent,  reportName, repo.getName(), this.getDelay()))
						.concat("<br/><br/>").concat(String.format(englishContent, reportName, repo.getName(), this.getDelay()));
			contact.setMessage(content);

			emailSender.sendNotification(contact);
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
	public void buildNotificationCheckUserPreference(String repositoryId, String subject, 
			String frenchContent, String englishContent, String message) {
		this.buildNotification(repositoryId, subject, frenchContent, englishContent, message, true);
	}
	
	/**
	 * Build ContactDto object and send notification
	 * @param repositoryId repository identifier
	 * @param subject
	 * @param frenchContent
	 * @param englishContent
	 * @param message optional
	 */
	public void buildNotificationMadatory(String repositoryId, String subject, 
			String frenchContent, String englishContent, String message) {
		this.buildNotification(repositoryId, subject, frenchContent, englishContent, message, false);
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
			String frenchContent, String englishContent, String message, boolean checkUserPreference) {
		// notification the report has been validated
		Repository repo = repositoryDao.findById(repositoryId);
		// List user id in DB
		List<String> repoUsersEmail = new ArrayList<String>();
		Set<String> repoUserIdList = repo.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
		for(String userId : repoUserIdList) {
			Optional<Profile> userProfile = profileDao.findById(userId);
			if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
				if(!checkUserPreference || (userProfile.get().getIsNotificationOff() == null || !userProfile.get().getIsNotificationOff())) {
					repoUsersEmail.add(userProfile.get().getEmail());
				}
			}
		}
		if( repoUsersEmail != null && repoUsersEmail.size() > 0) {
			ContactDto contact = new  ContactDto();
			Set<String> to = new HashSet<String>();
			to.addAll(repoUsersEmail);
			contact.setTo(to);
			contact.setSubject(String.format(subject, repo.getName(), repo.getName()));
			String content = appConfig.getEnglishHeader().concat("<br/><br/>");
			if(message != null) {
				content = content.concat(String.format(frenchContent, repo.getName(), message))
						.concat("<br/><br/>").concat(String.format(englishContent, repo.getName(), message));	

			} else {
				content = content.concat(String.format(frenchContent, repo.getName()))
						.concat("<br/><br/>").concat(String.format(englishContent, repo.getName()));
			}
			contact.setMessage(content);

			emailSender.sendNotification(contact);
		}
	}

}
