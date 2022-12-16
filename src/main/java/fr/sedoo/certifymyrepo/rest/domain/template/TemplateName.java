package fr.sedoo.certifymyrepo.rest.domain.template;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateName implements Comparable<TemplateName>{
	
	public TemplateName(String id, String name) {
		this.setId(id);
		this.setName(name);
	}
	
	private String id;
	private String name;
	
	@Override
	public int compareTo(TemplateName arg) {
		return arg.getId().compareTo(this.getId());
	}

}
