package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Affiliation;

@Component
public class AffiliationDaoMongoImpl implements AffiliationDao {

	@Autowired
	AffiliationRepository affiliationRepository;

	@Override
	public Affiliation save(Affiliation affiliation) {
		return affiliationRepository.save(affiliation);
	}

	@Override
	public List<Affiliation> findAll() {
		return affiliationRepository.findAll();
	}

	@Override
	public Affiliation findById(String id) {
		return affiliationRepository.findById(id).get();
	}

}
