package fr.sedoo.certifymyrepo.rest.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Document(collection = Profile.COLLECTION_NAME, language = "english")
@TypeAlias("Profile")
public class Profile {
	
	public final static String COLLECTION_NAME = "profiles";

	@Id
	private String id;
	private String orcid;
	private String name;
	private String email;
}
