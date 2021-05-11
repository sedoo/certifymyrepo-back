package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullRepositoryDto {
	
	private String name;
	private RepositoryDto repository;
	private RepositoryHealth healthLatestValidReport;
	private RepositoryHealth healthLatestInProgressReport;
	private boolean isReadonly;

}
