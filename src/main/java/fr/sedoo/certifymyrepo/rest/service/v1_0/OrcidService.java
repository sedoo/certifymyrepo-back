package fr.sedoo.certifymyrepo.rest.service.v1_0;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.dao.OrcidDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;

@RestController
@CrossOrigin
@RequestMapping(value = "/orcid/v1_0")
public class OrcidService {
	
	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	OrcidDao orcidDao;
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getUserByOrcId/{orcid}", method = RequestMethod.GET)
	public Profile getUserByOrcId(@RequestHeader("Authorization") String authHeader, @PathVariable String orcid) {
		Profile userProfile = profileDao.findByOrcid(orcid);
		if(userProfile == null) {
			ProfileDto user = orcidDao.getUserInfoByOrcid(orcid);
			if(user != null) {
				userProfile = new Profile();
				userProfile.setOrcid(orcid);
				userProfile.setName(user.getName());
				userProfile.setEmail(user.getEmail());
			}
		}
		return userProfile;
	}

}
