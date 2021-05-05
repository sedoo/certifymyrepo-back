package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileDto {
	
	private String id;
	private String adminId;
	private String orcid;
	private String name;
	private String email;
}
