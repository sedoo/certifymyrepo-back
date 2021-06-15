package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessRequest {
	
	private String repositoryId;
	private String userId;
	private String userName;
	private String orcid;
	private String email;
	private String role;
	private String text;

}
