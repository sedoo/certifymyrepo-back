package fr.sedoo.certifymyrepo.rest.domain;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnectedUser implements Comparable<ConnectedUser>{
	
	public ConnectedUser(String userId, String fullName, String shortName, boolean isReadOnly, Date creationDate) {
		this.setUserId(userId);
		this.setFullName(fullName);
		this.setShortName(shortName);
		this.setReadOnly(isReadOnly);
		this.setCreationDate(creationDate);
	}
	
	private String userId;
	private String fullName;
	private String shortName;
	private boolean isReadOnly;
	private Date creationDate;
	
	@Override
	public int compareTo(ConnectedUser arg0) {
		// TODO Auto-generated method stub
		return this.getCreationDate().compareTo(arg0.getCreationDate());
	}

}
