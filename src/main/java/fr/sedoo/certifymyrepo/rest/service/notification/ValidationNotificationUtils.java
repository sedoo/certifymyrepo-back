package fr.sedoo.certifymyrepo.rest.service.notification;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.Profiles;

@Component
@Profile(Profiles.DIFFERENT_THAN_PRODUCTION_PROFILE)
public class ValidationNotificationUtils implements NotificationUtils {

	@Override
	public void buildNotificationMadatory(String repositoryId, String subject, String frenchContent,
			String englishContent, String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildNotificationCheckUserPreference(String repositoryId, String subject, String frenchContent,
			String englishContent, String message) {
		// TODO Auto-generated method stub

	}

}
