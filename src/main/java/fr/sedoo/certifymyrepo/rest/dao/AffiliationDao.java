package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Affiliation;

public interface AffiliationDao {

	Affiliation save(Affiliation affiliation);

	List<Affiliation> findAll();

	Affiliation findById(String id);

}
