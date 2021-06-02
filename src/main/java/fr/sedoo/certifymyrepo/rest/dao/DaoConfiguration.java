package fr.sedoo.certifymyrepo.rest.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DaoConfiguration {

	@Autowired
	AffiliationDao proxyAffiliationDao;

	@Autowired
	AttachmentDao proxyAttachmentDao;

	@Bean
	@Primary
	AffiliationDao getAffiliationDao() {
		return new AffiliationCachedDao(proxyAffiliationDao);
	}

	@Bean
	@Primary
	AttachmentDao getAttachementDao() {
		return new AttachmentCachedDao(proxyAttachmentDao);
	}

}
