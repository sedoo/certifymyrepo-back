package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUser {
	
	public RepositoryUser() {
		
	}
	
	public RepositoryUser(String id, String orcid, String name, String role) {
		super();
		this.id = id;
		this.name = name;
		this.role = role;
	}
	private String id;
	private String name;
	private String role;

}
