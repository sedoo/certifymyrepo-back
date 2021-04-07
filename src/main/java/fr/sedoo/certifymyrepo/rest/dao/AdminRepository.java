package fr.sedoo.certifymyrepo.rest.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import fr.sedoo.certifymyrepo.rest.domain.Admin;

public interface AdminRepository extends MongoRepository<Admin, String> {
	
	Admin findByUserId(String userId);

}
