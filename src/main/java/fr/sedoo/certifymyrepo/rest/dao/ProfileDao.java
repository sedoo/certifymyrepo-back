package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Profile;

public interface ProfileDao {

	Profile findById(String id);
	Profile findByOrcid(String orcid);
	Profile findByEmail(String email);
	List<Profile> findByOrcidIn(List<String> orcidList);
	List<Profile> findAll();
	List<Profile> findByNameOrEmail(String regex);
	
	Profile save(Profile profile);
	
	long count();

	void delete(String id);

}
