package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Profile;

@Component
public class ProfileDaoMongoImpl implements ProfileDao {

	@Autowired
	ProfileRepository profileRepository;

	@Override
	public Optional<Profile> findById(String id) {
		Optional<Profile> profile = profileRepository.findById(id);
		return profile;
	}

	@Override
	public Profile save(Profile profile) {
		if(profile.getEmail() != null) {
			profile.setEmail(profile.getEmail().toLowerCase());
		}
		return profileRepository.save(profile);
	}

	@Override
	public long count() {
		return profileRepository.count();
	}

	@Override
	public List<Profile> findByOrcidIn(List<String> userIdList) {
		return profileRepository.findByIdIn(userIdList);
	}

	@Override
	public Profile findByOrcid(String orcid) {
		return profileRepository.findByOrcid(orcid);
	}

	@Override
	public List<Profile> findAll() {
		return profileRepository.findAll();
	}

	@Override
	public void delete(String id) {
		profileRepository.deleteById(id);
	}

	@Override
	public List<Profile> findByNameOrEmail(String regex) {
		return profileRepository.findByNameOrEmailRegex(regex);
	}

	@Override
	public Profile findByEmail(String email) {
		return profileRepository.findByEmail(email.toLowerCase());
	}

	@Override
	public Profile findBySsoId(String ssoId) {
		return profileRepository.findBySsoId(ssoId);
	}

}
