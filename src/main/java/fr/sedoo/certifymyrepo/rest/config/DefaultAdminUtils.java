package fr.sedoo.certifymyrepo.rest.config;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.AdminDaoMongoImpl;
import fr.sedoo.certifymyrepo.rest.dao.OrcidDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.dto.UserLigth;

@Component
public class DefaultAdminUtils {
	
	private static final Logger LOG = LoggerFactory.getLogger(AdminDaoMongoImpl.class);
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private AdminDao adminDao;
	
	@Autowired
	private OrcidDao orcidDao;
	
	@Value("${ADMIN_ORCID_LIST}")
	private String[] admins;

	@Value("${SUPER_ADMIN_ORCID_LIST}")
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
		Profile user = profileDao.findByOrcid(orcid);
		if(user != null) {
			Admin admin = adminDao.findByUserId(user.getId());
			if(admin != null) {
				admin.setSuperAdmin(isSuperAdmin);
				adminDao.save(admin);
			} else {
				Admin entity = new Admin();
				entity.setName(user.getName());
				entity.setUserId(user.getId());
				entity.setSuperAdmin(isSuperAdmin);
				adminDao.save(entity);
			}
			if(isSuperAdmin) {
				LOG.info("{} has been updated as Super Admin", user.getName());
			} else {
				LOG.info("{} has been updated as Admin", user.getName());
			}
		} else {
			UserLigth userLigth = orcidDao.getUserInfoByOrcid(orcid);
			if(userLigth != null){	
				user = new Profile();
				user.setOrcid(orcid);
				user.setName(userLigth.getName());
				user.setEmail(userLigth.getEmail());
				user = profileDao.save(user);
				Admin entity = new Admin();
				entity.setName(user.getName());
				entity.setUserId(user.getId());
				entity.setSuperAdmin(isSuperAdmin);
				adminDao.save(entity);
				if(isSuperAdmin) {
					LOG.info("{} has been added as Super Admin", userLigth.getName());
				} else {
					LOG.info("{} has been added as Admin", userLigth.getName());
				}
			}
		}
	}
	
	/**
	 * Load profile user from json resources file
	 * @param name file name
	 * @return Array of user profile
	 */
	private Profile[] loadProfiles(String name) {
		Profile[] result = null;
		InputStream inJson = null;
		ClassLoader classLoader = getClass().getClassLoader();
		try {
			inJson = classLoader.getResourceAsStream(name);
			if(inJson != null ) {
				result = new ObjectMapper().readValue(inJson, Profile[].class);
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
