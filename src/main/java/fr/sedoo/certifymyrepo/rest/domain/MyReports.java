package fr.sedoo.certifymyrepo.rest.domain;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.dto.ReportDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyReports {
	
	List<ReportDto> reports;
	boolean isEditExistingAllowed;
	boolean isCreationValidationAllowed;

}
