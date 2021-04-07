package fr.sedoo.certifymyrepo.rest.dao;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;

public interface CertificationReportTemplateDao {
	
	CertificationTemplate getCertificationReportTemplate(String name);

}
