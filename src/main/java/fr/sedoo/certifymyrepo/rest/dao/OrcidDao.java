package fr.sedoo.certifymyrepo.rest.dao;

import fr.sedoo.certifymyrepo.rest.dto.User;

public interface OrcidDao {
	
	User getUserInfoByOrcid(String orcid);

}
