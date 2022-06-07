package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectedUser {
	
	public ConnectedUser(String userId, String fullName, String shortName) {
		this.setUserId(userId);
		this.setFullName(fullName);
		this.setShortName(shortName);
	}
	
	private String userId;
	private String fullName;
	private String shortName;


}
