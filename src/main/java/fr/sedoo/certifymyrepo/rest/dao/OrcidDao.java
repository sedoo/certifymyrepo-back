package fr.sedoo.certifymyrepo.rest.dao;

import fr.sedoo.certifymyrepo.rest.dto.UserLigth;

public interface OrcidDao {
	
	UserLigth getUserInfoByOrcid(String orcid);

}
