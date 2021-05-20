package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUser {
	
	public RepositoryUser() {
		
	}
	
	public RepositoryUser(String id, String role) {
		super();
		this.id = id;
		this.role = role;
	}
	private String id;
	private String role;

}
