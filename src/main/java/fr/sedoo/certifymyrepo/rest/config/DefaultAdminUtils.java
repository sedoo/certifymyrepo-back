package fr.sedoo.certifymyrepo.rest.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.AdminDaoMongoImpl;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.Profile;

@Component
public class DefaultAdminUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(AdminDaoMongoImpl.class);
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private AdminDao adminDao;
	
	@Value("${orcid.admins}")
	private String[] admins;

	@Value("${orcid.superAdmins}")
	private String[] superAdmins;
	
	/**
	 * Initialize Super admin and admin
	 */
	public void init() {
		for(String adminOrcid : admins) {
			saveAdmin(adminOrcid, false);
		}
		for(String adminOrcid : superAdmins) {
			saveAdmin(adminOrcid, true);
		}
	}
	
	/**
	 * Insure given profiles exist and are Super Admin or Admin
	 * @param profiles
	 * @param isSuperAdmin
	 */
	private void saveAdmin(String orcid, boolean isSuperAdmin) {
		Profile userProfile = profileDao.findByOrcid(orcid);
		if(userProfile != null) {
			Admin admin = adminDao.findByUserId(userProfile.getId());
			if(admin != null) {
				admin.setSuperAdmin(isSuperAdmin);
				adminDao.save(admin);
			} else {
				Admin entity = new Admin();
				entity.setName(userProfile.getName());
				entity.setUserId(userProfile.getId());
				entity.setSuperAdmin(isSuperAdmin);
				adminDao.save(entity);
			}
			if(isSuperAdmin) {
				LOG.info("{} has been updated as Super Admin", userProfile.getName());
			} else {
				LOG.info("{} has been updated as Admin", userProfile.getName());
			}
		}
	}

}
