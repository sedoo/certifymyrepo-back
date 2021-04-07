package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import fr.sedoo.certifymyrepo.rest.domain.Profile;

public interface ProfileRepository extends MongoRepository<Profile, String> {
	
	Profile findByOrcid(String orcid);
	Profile findByEmail(String email);
	List<Profile> findByIdIn(List<String> idList);
	
	@Query(value = "{$or:[{'name':{$regex : ?0, $options: 'i'}},{'email':{$regex : ?0, $options: 'i'}}]}")
	List<Profile> findByNameOrEmailRegex(String regex);

}
