package fr.sedoo.certifymyrepo.rest.export;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Requirement {
	
	private String code;
	@JsonIgnore
	private String requirementLabel;
	private String response;
	private String level;
	@JsonIgnore
	private String levelLabel;
	private List<String> attachments;
	@JsonIgnore
	private List<CommentExport> comments;

}
