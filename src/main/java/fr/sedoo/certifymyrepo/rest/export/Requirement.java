package fr.sedoo.certifymyrepo.rest.export;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Requirement {
	
	private String code;
	@JsonIgnore
	private String requirementLabel;
	@JacksonXmlCData
	private String response;
	private String level;
	@JsonIgnore
	private String levelLabel;
	private List<String> attachments;
	@JsonIgnore
	private List<CommentExport> comments;

}
