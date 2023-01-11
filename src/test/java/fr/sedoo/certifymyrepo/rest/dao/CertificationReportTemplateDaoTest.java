package fr.sedoo.certifymyrepo.rest.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.TemplateName;

@RunWith(SpringJUnit4ClassRunner.class)
public class CertificationReportTemplateDaoTest {
	
	CertificationReportTemplateDao dao = new CertificationReportTemplateDaoImpl();
	
	@Test
	public void testGetTemplate() {
		CertificationTemplate result = dao.getCertificationReportTemplate("CTS-2020-2022-fr");
		assertEquals("CTS-2020-2022 en français", result.getName());
	}
	
	@Test
	public void testTemplateList() {
		List<TemplateName> list = dao.getTemplateNameList();
		assertEquals(4, list.size());
		assertEquals("CTS-2023-2025-fr", list.get(0).getId());
		assertEquals("CTS-2023-2025-en", list.get(1).getId());
		assertEquals("CTS-2020-2022-fr", list.get(2).getId());
		assertEquals("CTS-2020-2022-en", list.get(3).getId());
	}

}
