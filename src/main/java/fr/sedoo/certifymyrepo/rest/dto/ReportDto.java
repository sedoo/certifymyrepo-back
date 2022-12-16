package fr.sedoo.certifymyrepo.rest.dto;

import java.util.Date;
import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDto {
	
	public ReportDto(CertificationReport report, CertificationTemplate template) {
		this.setId(report.getId());
		this.setRepositoryId(report.getRepositoryId());
		this.setStatus(report.getStatus());
		this.setVersion(report.getVersion());
		this.setUpdateDate(report.getUpdateDate());
		
		this.setTemplateName(template.getName());
		if(template != null && template.getLevels() != null) {
			this.setLevelMaxValue(template.getLevels().size()-1);
		}

	}
	
	private String id;
	private String templateName;
	/** 
	 * The maximum value of radar chart yaxis
	 */
	private int levelMaxValue;
	private String repositoryId;
	private List<CertificationItemDto> items;
	private String version;
	private ReportStatus status;
	private Date updateDate;

}
