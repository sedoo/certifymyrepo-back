package fr.sedoo.certifymyrepo.rest.export;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import fr.sedoo.certifymyrepo.rest.dto.CommentDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentExport {
	
	public CommentExport(CommentDto comment, String language) {
		if(comment.getUserName() != null) {
			this.setUserName(comment.getUserName());
		} else {
	        Locale locale = new Locale(language);
	        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
			this.setUserName(messages.getString("export.comment.user.deleted"));
		}
		this.setValue(comment.getText());
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		this.setCreationDate(df.format(comment.getCreationDate()));
	}
	
	private String userName;
	private String value;
	private String creationDate;

}
