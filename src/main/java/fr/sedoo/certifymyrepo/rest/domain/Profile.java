package fr.sedoo.certifymyrepo.rest.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Document(collection = Profile.COLLECTION_NAME, language = "english")
public class Profile {
	
	public final static String COLLECTION_NAME = "profiles";

	@Id
	private String id;
	private String orcid;
	private String name;
	private String email;
	private String title;
	private String fax;
	private List<String> phones = new ArrayList<>();
}
