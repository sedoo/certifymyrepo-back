package fr.sedoo.certifymyrepo.rest.domain;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = RequirementComments.COMMENTS_COLLECTION_NAME, language = "english")
public class RequirementComments {
	
	public final static String COMMENTS_COLLECTION_NAME = "comments";
	
	@Id
	private String id;
	private String reportId;
	private String itemCode;
	private List<Comment> comments;

}
