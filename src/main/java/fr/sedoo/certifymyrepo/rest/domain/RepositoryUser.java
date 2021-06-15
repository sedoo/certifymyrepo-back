package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUser {
	
	public static final String ACTIVE = "ACTIVE";
	public static final String PENDING = "PENDING";
	
	public RepositoryUser() {
		
	}
	
	public RepositoryUser(String id, String role) {
		super();
		this.id = id;
		this.role = role;
		this.status = RepositoryUser.ACTIVE;
	}
	public RepositoryUser(String id, String role, String status) {
		super();
		this.id = id;
		this.role = role;
		this.status = status;
	}
	private String id;
	private String role;
	private String status;

}
