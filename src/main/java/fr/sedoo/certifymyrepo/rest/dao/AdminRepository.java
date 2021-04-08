package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import fr.sedoo.certifymyrepo.rest.domain.Admin;

public interface AdminRepository extends MongoRepository<Admin, String> {
	
	Admin findByUserId(String userId);
	
	@Query(value = "{ 'isSuperAdmin' : true }")
	List<Admin> findAllSuperAdmin();

}
