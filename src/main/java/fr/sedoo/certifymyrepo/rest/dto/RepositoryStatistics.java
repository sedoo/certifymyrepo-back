package fr.sedoo.certifymyrepo.rest.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepositoryStatistics {
	
	private String repositoryName;
	private long inProgressReportNumber;
	private long validatedReportNumber;
	private Map<String, Long> userNumber;

}
