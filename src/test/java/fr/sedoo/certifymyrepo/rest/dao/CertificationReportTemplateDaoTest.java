package fr.sedoo.certifymyrepo.rest.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
public class CertificationReportTemplateDaoTest {
	
	private static final String TEMPLATE_NAME = "CTS-2020-2022";
	@Test
	public void test() {
		CertificationReportTemplateDao certificationReportTemplateDao = new CertificationReportTemplateDaoImpl();
		CertificationTemplate template = certificationReportTemplateDao.getCertificationReportTemplate(TEMPLATE_NAME);
		assertEquals(TEMPLATE_NAME, template.getName());
	}

}
