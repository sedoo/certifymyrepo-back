package fr.sedoo.certifymyrepo.rest.dao;

import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;

public interface OrcidDao {
	
	ProfileDto getUserInfoByOrcid(String orcid);

}
