package fr.sedoo.certifymyrepo.rest.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
public class CertificationReportTemplateDaoTest {
	
	@Test
	public void test() {
		CertificationReportTemplateDao dao = new CertificationReportTemplateDaoImpl();
		CertificationTemplate result = dao.getCertificationReportTemplate("CTS-2020-2022-fr");
		assertEquals("CTS-2020-2022 en français", result.getName());
	}

}
