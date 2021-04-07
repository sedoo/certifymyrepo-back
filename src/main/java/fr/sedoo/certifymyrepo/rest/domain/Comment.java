package fr.sedoo.certifymyrepo.rest.domain;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Comment {
	
	private Integer id;
	private String user;
	private String text;
	private Date creationDate;

}
