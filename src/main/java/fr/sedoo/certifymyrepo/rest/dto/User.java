package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
	
	private String userId;
	private String adminId;
	private String name;
	private String email;
	private boolean isAdmin;

}
