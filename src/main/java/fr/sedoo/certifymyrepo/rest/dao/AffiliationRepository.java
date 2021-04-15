package fr.sedoo.certifymyrepo.rest.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import fr.sedoo.certifymyrepo.rest.domain.Affiliation;

public interface AffiliationRepository extends MongoRepository<Affiliation, String> {

}
