package fr.sedoo.certifymyrepo.rest.dto;

import java.util.Date;
import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDto {
	
	public ReportDto(CertificationReport report) {
		this.setId(report.getId());
		this.setTemplateId(report.getTemplateId());
		this.setRepositoryId(report.getRepositoryId());
		this.setStatus(report.getStatus());
		this.setVersion(report.getVersion());
		this.setUpdateDate(report.getUpdateDate());
	}
	
	private String id;
	private String templateId;
	private int levelMaxValue;
	private String repositoryId;
	private List<CertificationItemDto> items;
	private String version;
	private ReportStatus status;
	private Date updateDate;

}
