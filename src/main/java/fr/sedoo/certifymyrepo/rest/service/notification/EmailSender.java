package fr.sedoo.certifymyrepo.rest.service.notification;

import javax.mail.internet.AddressException;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public interface EmailSender {

	void send(SimpleEmail email, String content) throws AddressException, EmailException;

}
