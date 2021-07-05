package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUserDto {
	
	public RepositoryUserDto() {
		
	}
	
	public RepositoryUserDto(String id, String name, String email, String role, String status) {
		super();
		this.setId(id);
		this.setName(name);
		this.setEmail(email);
		this.setRole(role);
		this.setStatus(status);
	}
	private String id;
	private String name;
	private String email;
	private String role;
	private String status;

}
