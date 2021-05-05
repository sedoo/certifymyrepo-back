package fr.sedoo.certifymyrepo.rest.domain.template;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementTemplate {
	
	private String code;
	private RequirementLabel requirement;
	private ResponseTemplate response;
	private boolean levelActive;

}
