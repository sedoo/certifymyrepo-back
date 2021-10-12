package fr.sedoo.certifymyrepo.rest.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullRepositoryDto {
	
	private String name;
	private Date creationDate;
	private RepositoryDto repository;
	private Date latestValidReportUpdateDate;
	private Date latestInProgressReportUpdateDate;
	private RepositoryHealth healthLatestValidReport;
	private RepositoryHealth healthLatestInProgressReport;
	private boolean isReadonly;

}
