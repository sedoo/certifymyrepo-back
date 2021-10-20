package fr.sedoo.certifymyrepo.rest.config;

import java.io.File;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ApplicationConfig {
	
	@Value("${tmpFolder}/download")
	private String temporaryDownloadFolderName;
	
	@Value("${temporary.storageDuration}")
	Integer storageDuration;
	
	@PostConstruct
	public void init() {
		File aux = new File(temporaryDownloadFolderName);
		if (!aux.exists()) {
			aux.mkdirs();
		}
	}
	
	
	@Value("${english.header}")
	private String englishHeader;
	/**
	 * Create user notification
	 */
	@Value("${create.user.notification.subject}")
	private String createUserNotificationSubject;
	@Value("${create.user.notification.orcid.fr.content}")
	private String createUserNotificationOrcidFrenchContent;
	@Value("${create.user.notification.renater.fr.content}")
	private String createUserNotificationRenaterFrenchContent;
	@Value("${create.user.notification.orcid.en.content}")
	private String createUserNotificationOrcidEnglishContent;
	@Value("${create.user.notification.renater.en.content}")
	private String createUserNotificationRenaterEnglishContent;
	
	/**
	 * Add an user on a repository
	 */
	@Value("${add.user.notification.subject}")
	private String addUserNotificationSubject;
	@Value("${add.user.notification.fr.content}")
	private String addUserNotificationFrenchContent;
	@Value("${add.user.notification.en.content}")
	private String addUserNotificationEnglishContent;
	
	/**
	 * Remove an user from a repository
	 */
	@Value("${remove.user.notification.subject}")
	private String removeUserNotificationSubject;
	@Value("${remove.user.notification.fr.content}")
	private String removeUserNotificationFrenchContent;
	@Value("${remove.user.notification.en.content}")
	private String removeUserNotificationEnglishContent;
	
	/**
	 * Access request declined
	 */
	@Value("${declined.user.notification.subject}")
	private String declinedUserAccessNotificationSubject;
	@Value("${declined.user.notification.fr.content}")
	private String declinedUserAccessNotificationFrenchContent;
	@Value("${declined.user.notification.en.content}")
	private String declinedUserAccessNotificationEnglishContent;
	
	/**
	 * Report validation
	 */
	@Value("${report.validation.notification.subject}")
	private String reportValidationNotificationSubject;
	@Value("${report.validation.notification.fr.content}")
	private String reportValidationNotificationFrenchContent;
	@Value("${report.validation.notification.en.content}")
	private String reportValidationNotificationEnglishContent;
	
	/**
	 * Report new version
	 */
	@Value("${report.new.version.notification.subject}")
	private String reportNewVersionNotificationSubject;
	@Value("${report.new.version.notification.fr.content}")
	private String reportNewVersionNotificationFrenchContent;
	@Value("${report.new.version.notification.en.content}")
	private String reportNewVersionNotificationEnglishContent;
	
	/**
	 * New comment on a requirement
	 */
	@Value("${new.comment.notification.subject}")
	private String newCommentNotificationSubject;
	@Value("${new.comment.notification.fr.content}")
	private String newCommentNotificationFrenchContent;
	@Value("${new.comment.notification.en.content}")
	private String newCommentNotificationEnglishContent;
	
	/**
	 * Repository access
	 */
	@Value("${repository.access.request.subject}")
	private String repositoryAccessSubject;
	@Value("${repository.access.request.fr.content}")
	private String repositoryAccessFrenchContent;
	@Value("${repository.access.request.en.content}")
	private String repositoryAccessEnglishContent;
	
	/**
	 * Reminder if no activity on a repository
	 */
	@Value("${no.activity.subject}")
	private String noActivityNotificationSubject;
	@Value("${no.activity.fr.content}")
	private String noActivityNotificationFrenchContent;
	@Value("${no.activity.en.content}")
	private String noActivityNotificationEnglishContent;

}
