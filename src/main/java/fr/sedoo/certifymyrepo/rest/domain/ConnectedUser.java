package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectedUser {
	
	public ConnectedUser(String userId, String fullName, String shortName, boolean isReadOnly) {
		this.setUserId(userId);
		this.setFullName(fullName);
		this.setShortName(shortName);
		this.setReadOnly(isReadOnly);
	}
	
	private String userId;
	private String fullName;
	private String shortName;
	private boolean isReadOnly;

}
