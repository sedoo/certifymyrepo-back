package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryHealth {
	
	private boolean isGreen;
	private boolean isOrange;
	private boolean isRed;
	private ReportDto latestReport;

}