package fr.sedoo.certifymyrepo.rest.dto;

import java.util.Date;

import fr.sedoo.certifymyrepo.rest.domain.Comment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
	
	public CommentDto(Comment comment, String username) {
		this.setId(comment.getId());
		this.setCreationDate(comment.getCreationDate());
		this.setUserId(comment.getUserId());
		this.setText(comment.getText());
		this.setUserName(username);
	}
	
	private Integer id;
	private String userId;
	private String userName;
	private String text;
	private Date creationDate;
}
