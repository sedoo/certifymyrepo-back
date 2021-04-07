package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import fr.sedoo.certifymyrepo.rest.domain.Repository;

public interface RepositoryRepository extends MongoRepository<Repository, String> {
	
	@Query(value = "{ 'users.id' :  ?0 }")
    List<Repository> findByUserIdsIn(String orcid);
	
	@Query(value = "{ 'id' : ?0, 'users.id' :  ?1 }")
	Repository findByIdAndUsersIdsIn(String id, String orcid);
	
	@Query(value = "{$or:[{'name':{$regex : ?0, $options: 'i'}},{'keywords':{$regex : ?0, $options: 'i'}}]}")
	List<Repository> findByNameOrKeywordsRegex(String regex);
	
	@Query(value = "{'name':{$regex: '^?0$', $options: 'i'}}")
	Repository findByNameCaseInsensitive(String name);

}
