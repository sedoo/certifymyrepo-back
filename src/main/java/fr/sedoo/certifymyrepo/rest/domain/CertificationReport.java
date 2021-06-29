package fr.sedoo.certifymyrepo.rest.domain;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = CertificationReport.REPOSITORY_COLLECTION_NAME, language = "english")
public class CertificationReport {
	
	public CertificationReport() {
		super();
	}
	
	public final static String REPOSITORY_COLLECTION_NAME = "certificationReport";
	
	@Id
	private String id;
	private String templateId;
	private String repositoryId;
	private List<CertificationItem> items;
	private String version;
	private ReportStatus status;
	private Date updateDate;
	private Date lastNotificationDate;

}
