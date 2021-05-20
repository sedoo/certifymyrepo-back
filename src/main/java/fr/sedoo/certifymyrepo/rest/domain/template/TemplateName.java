package fr.sedoo.certifymyrepo.rest.domain.template;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateName {
	
	public TemplateName(String id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	private String id;
	private String name;

}
