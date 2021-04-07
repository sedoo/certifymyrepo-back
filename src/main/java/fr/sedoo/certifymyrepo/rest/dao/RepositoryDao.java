package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Repository;

public interface RepositoryDao {
	
	List<Repository> findAll();
	List<Repository> findAllByUserId(String userId);
	
	Repository findById(String id);
	Repository findByName(String name);
	
	List<Repository> findByNameOrKeywords(String regex);
	
	Repository findByIdAndUserId(String id, String orcid);

	Repository save(Repository repository);
	
	void delete(String id);
	long count();

}
