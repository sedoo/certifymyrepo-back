package fr.sedoo.certifymyrepo.rest.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyReports {
	
	List<CertificationReport> reports;
	boolean isEditExistingAllowed;
	boolean isCreationValidationAllowed;

}
