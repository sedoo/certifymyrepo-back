package fr.sedoo.certifymyrepo.rest.domain;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryHealth {
	
	private boolean isGreen;
	private boolean isOrange;
	private boolean isRed;
	private Date lastUpdateDate;
	private int numberOfLevel;
	private List<String> requirementCodeList;
	private List<String> requirementLevelList;

}