package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryUser {
	
	private String id;
	private String orcid;
	private String name;
	private String role;

}
