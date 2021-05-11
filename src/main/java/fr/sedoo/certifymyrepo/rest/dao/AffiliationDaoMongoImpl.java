package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;
import java.util.Optional;

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
		Optional<Affiliation> affiliation = affiliationRepository.findById(id);
		if(affiliation.isPresent()) {
			return affiliationRepository.findById(id).get();
		} else {
			return null;
		}

	}

	@Override
	public void deleteById(String id) {
		affiliationRepository.deleteById(id);
	}

}
