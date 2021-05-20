package fr.sedoo.certifymyrepo.rest.export;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Requirement {
	
	private String requirement;
	private String response;
	private String levelLabel;
	private List<String> attachments;
	private List<CommentDto> comments;

}
