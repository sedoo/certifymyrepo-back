package fr.sedoo.certifymyrepo.rest.service.notification;

public interface NotificationUtils {
	
	/**
	 * Build ContactDto object and send notification
	 * @param repositoryId repository identifier
	 * @param subject
	 * @param frenchContent
	 * @param englishContent
	 * @param message optional
	 */
	void buildNotificationMadatory(String repositoryId, String subject, 
			String frenchContent, String englishContent, String message);
	
	/**
	 * Build ContactDto object and send notification
	 * @param repositoryId repository identifier
	 * @param subject
	 * @param frenchContent
	 * @param englishContent
	 * @param message optional
	 */
	void buildNotificationCheckUserPreference(String repositoryId, String subject, 
			String frenchContent, String englishContent, String message);

}
