package fr.sedoo.certifymyrepo.rest.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = Repository.REPOSITORY_COLLECTION_NAME, language = "english")
@TypeAlias("Repository")
public class Repository {
	
	public final static String REPOSITORY_COLLECTION_NAME = "repositories";
	
	@Id
	private String id;
	private String name;
	private Date creationDate;
	private String affiliationId;
	private List<String> keywords;
	private String contact;
	private List<RepositoryUser> users;
	private String url;
	private String description;

}
