package fr.sedoo.certifymyrepo.rest.dto;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Repository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryDto {
	
	public RepositoryDto(Repository repository, AffiliationDto affiliation) {
		this.setId(repository.getId());
		this.setName(repository.getName());
		this.setAffiliation(affiliation);
		this.setKeywords(repository.getKeywords());
		this.setContact(repository.getContact());
		this.setUsers(repository.getUsers());
		this.setUrl(repository.getUrl());
		this.setDescription(repository.getDescription());
	}
	private String id;
	private String name;
	private AffiliationDto affiliation;
	private List<String> keywords;
	private String contact;
	private List<RepositoryUser> users;
	private String url;
	private String description;

}
