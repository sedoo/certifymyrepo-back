package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUserDto {
	
	public RepositoryUserDto() {
		
	}
	
	public RepositoryUserDto(String id, String name, String role, String status) {
		super();
		this.id = id;
		this.name = name;
		this.role = role;
		this.status = status;
	}
	private String id;
	private String name;
	private String role;
	private String status;

}
