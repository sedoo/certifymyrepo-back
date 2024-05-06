package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;
import java.util.Optional;

import fr.sedoo.certifymyrepo.rest.domain.Profile;

public interface ProfileDao {

	Optional<Profile> findById(String id);
	Profile findByOrcid(String orcid);
	Profile findByEmail(String email);
	Profile findBySsoId(String ssoId);
	List<Profile> findByOrcidIn(List<String> orcidList);
	List<Profile> findAll();
	List<Profile> findByNameOrEmail(String regex);
	
	Profile save(Profile profile);
	
	long count();

	void delete(String id);

}
