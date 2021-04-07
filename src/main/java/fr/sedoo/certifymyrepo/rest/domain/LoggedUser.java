package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoggedUser {
	
	private String token;
	private boolean isAdmin;
	private boolean isSuperAdmin;
	private Profile profile;

}
