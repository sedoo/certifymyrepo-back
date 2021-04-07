package fr.sedoo.certifymyrepo.rest.dao;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;

@Component
public class CertificationReportTemplateDaoImpl implements CertificationReportTemplateDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(CertificationReportTemplateDaoImpl.class);

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

}
