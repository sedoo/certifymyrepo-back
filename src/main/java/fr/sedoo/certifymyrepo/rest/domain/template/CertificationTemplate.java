package fr.sedoo.certifymyrepo.rest.domain.template;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificationTemplate {
	
	private String name;
	private Description description;
	private List<LevelTemplate> levels;
	private List<RequirementTemplate> requirements;

}
