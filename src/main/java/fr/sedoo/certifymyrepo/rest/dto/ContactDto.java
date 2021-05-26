package fr.sedoo.certifymyrepo.rest.dto;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactDto {
	
	private Set<String> to;
	private String fromName;
	private String fromEmail;
	private String subject;
	private String message;

}
