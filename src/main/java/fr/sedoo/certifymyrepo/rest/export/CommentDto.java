package fr.sedoo.certifymyrepo.rest.export;

import java.text.SimpleDateFormat;

import fr.sedoo.certifymyrepo.rest.domain.Comment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
	
	public CommentDto(Comment comment) {
		this.setUserName(comment.getUserName());
		this.setValue(comment.getText());
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.setCreationDate(df.format(comment.getCreationDate()));
	}
	
	private String userName;
	private String value;
	private String creationDate;

}
