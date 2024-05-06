package fr.sedoo.certifymyrepo.rest.config;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.sso.utils.jwt.Credential;
import fr.sedoo.sso.utils.jwt.JwtUtils;
import fr.sedoo.sso.utils.token.RoleProvider;

@Component("permissionEvaluator")
public class SsoPermissionEvaluator  {
	
	@Autowired
	ApplicationConfig config;
	
	@Autowired
	SsoKeyLoader keyLoader;
	
	@Autowired
	ProfileDao profileDoa;

	/**
	 * We just check the user has got one of the admin roles.
	 * @param request
	 * @return
	 */
	
	public Credential getCredentials(HttpServletRequest request) {
		return JwtUtils.getCredential(request, keyLoader.getPublicKeys());
	}
	
	public boolean isAdmin(HttpServletRequest request) {
		Set<String> roles = new HashSet<>();
		try {
			roles = RoleProvider.getRolesFromHttpRequest(request, keyLoader.getPublicKeys());
		}
		catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		if (CollectionUtils.isEmpty(roles)) {
			return false;
		}
		else {
			 if (JwtUtils.hasAtLeastOneRole(config.getAdminRoles(), roles)) {
				 return true;
			 }
			 else {
				 return false;
			 }
		}
	}
	
	/**
	 * We just check the user has got one of the user roles.
	 * @param request
	 * @return
	 */
	
	public boolean isUser(HttpServletRequest request) {
		Set<String> roles = new HashSet<>();
		try {
			roles = RoleProvider.getRolesFromHttpRequest(request, keyLoader.getPublicKeys());
		}
		catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
		}
		if (CollectionUtils.isEmpty(roles)) {
			return false;
		}
		else {
			 if (JwtUtils.hasAtLeastOneRole(config.getUserRoles(), roles)) {
				 return true;
			 }
			 else {
				 return false;
			 }
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

