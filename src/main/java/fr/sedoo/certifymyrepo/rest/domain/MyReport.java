package fr.sedoo.certifymyrepo.rest.domain;

import java.util.List;
import java.util.Map;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.dto.RequirementCommentsDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyReport {
	
	CertificationReport report;
	CertificationTemplate template;
	List<RequirementCommentsDto> requirementComments;
	boolean isEditExistingAllowed;
	boolean isValidationAllowed;
	Map<String, List<String>> attachments;

}
