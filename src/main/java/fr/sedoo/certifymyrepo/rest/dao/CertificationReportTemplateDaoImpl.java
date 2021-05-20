package fr.sedoo.certifymyrepo.rest.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.TemplateName;

@Component
public class CertificationReportTemplateDaoImpl implements CertificationReportTemplateDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(CertificationReportTemplateDaoImpl.class);
	
	@Autowired
	private ResourceLoader resourceLoader;

	@Override
	public CertificationTemplate getCertificationReportTemplate(String name) {
		CertificationTemplate result = null;
		InputStream inJson = null;
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			inJson = classLoader.getResourceAsStream("certificationReportTemplate/".concat(name).concat(".json"));
			if(inJson != null ) {
				result = new ObjectMapper().readValue(inJson, CertificationTemplate.class);
			}
		} catch (IOException e) {
			LOG.error("Load file", e);;
		} finally {
			if(inJson != null) {
				try {
					inJson.close();
				} catch (IOException e) {
					LOG.error("Error while close stream", e);
				}
			}
		}
		return result;
	}

	@Override
	public List<TemplateName> getTemplateNameList() {
		List<TemplateName> fileList = new ArrayList<TemplateName>();
		String contactResourcePath = "classpath:certificationReportTemplate/*.json";
		Resource[] resources;
		try {
			resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(contactResourcePath);
			for(Resource resource : resources) {
				String templateId = resource.getFilename();
				if(templateId.contains(".json")) {
					templateId = templateId.replace(".json", "");
				}
				CertificationTemplate template = this.getCertificationReportTemplate(templateId);
				fileList.add(new TemplateName(templateId, template.getName()));
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return fileList;
	}

}
