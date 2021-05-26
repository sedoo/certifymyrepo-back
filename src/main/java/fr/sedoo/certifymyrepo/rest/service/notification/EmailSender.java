package fr.sedoo.certifymyrepo.rest.service.notification;

import fr.sedoo.certifymyrepo.rest.dto.ContactDto;

public interface EmailSender {
	
	boolean sendNotification(ContactDto contact);

}
