package fr.sedoo.certifymyrepo.rest.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.sso.utils.jwt.Credential;
import fr.sedoo.sso.utils.jwt.JwtUtils;

@Component("permissionEvaluator")
public class SsoPermissionEvaluator  {
	
	@Autowired
	ApplicationConfig config;
	
	@Autowired
	SsoKeyLoader keyLoader;
	
	@Autowired
	ProfileDao profileDoa;
	
	@Autowired
	AdminDao adminDao;

	/**
	 * We just check the user has got one of the admin roles.
	 * @param request
	 * @return
	 */
	
	public Credential getCredentials(HttpServletRequest request) {
		return JwtUtils.getCredential(request, keyLoader.getPublicKeys());
	}
	
	public boolean isAdmin(HttpServletRequest request) {
		Profile user = this.getUser(request);
		if(user != null) {
			return adminDao.isAdmin(user.getId()) || adminDao.isSuperAdmin(user.getId());
		} else {
			return false;
		}
	}
	
	/**
	 * We just check the user has got one of the user roles.
	 * @param request
	 * @return
	 */
	
	public boolean isUser(HttpServletRequest request) {
		Profile user = this.getUser(request);
		if(user != null) {
			return true;
		} else {
			return false;
		}

	}
	
	public String getSsoId(HttpServletRequest request) {
		Credential credential = JwtUtils.getCredential(request, keyLoader.getPublicKeys());
		if(credential != null) {
			return credential.getId();
		} else {
			return null;
		}
	}
	
	public Profile getUser(HttpServletRequest request) {
		Profile profile = null;
		Credential credential = JwtUtils.getCredential(request, keyLoader.getPublicKeys());
		if(credential != null) {
			profile = profileDoa.findBySsoId(credential.getId());
			if(profile == null) {
				if(credential.getEmail() != null) {
					profile = profileDoa.findByEmail(credential.getEmail());
					if(profile != null) {
						profile.setSsoId(credential.getId());	
					} else {
						profile = new Profile();
						profile.setEmail(credential.getEmail());
						profile.setName(credential.getName());
						profile.setSsoId(credential.getId());
					}
					profileDoa.save(profile);
				}
			}
			
		}
		return profile;
	}
	
		
}

