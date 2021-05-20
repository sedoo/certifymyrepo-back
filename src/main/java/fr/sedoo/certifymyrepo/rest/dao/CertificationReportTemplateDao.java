package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.TemplateName;

public interface CertificationReportTemplateDao {
	
	CertificationTemplate getCertificationReportTemplate(String name);
	List<TemplateName> getTemplateNameList();

}
