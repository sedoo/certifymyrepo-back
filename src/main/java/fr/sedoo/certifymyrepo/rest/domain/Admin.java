package fr.sedoo.certifymyrepo.rest.domain;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = Admin.ADMIN_COLLECTION_NAME, language = "english")
@TypeAlias("Admin")
public class Admin {

	public final static String ADMIN_COLLECTION_NAME = "admins";
	
	@Id
	private String id;
	private String name;
	private String userId;
	private boolean isSuperAdmin;
}
