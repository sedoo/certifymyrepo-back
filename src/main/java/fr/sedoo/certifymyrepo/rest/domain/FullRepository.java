package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullRepository {
	
	private Repository repository;
	private RepositoryHealth healthLatestValidReport;
	private RepositoryHealth healthLatestInProgressReport;
	private boolean isReadonly;

}
