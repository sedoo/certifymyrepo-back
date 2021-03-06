package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Repository;

@Component
public class RepositoryDaoMongoImpl implements RepositoryDao{
	
	Logger logger = LoggerFactory.getLogger(RepositoryDaoMongoImpl.class);

	@Autowired
	RepositoryRepository repositoryRepository;
	
	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<Repository> findAll() {
		return repositoryRepository.findAll();
	}

	@Override
	public Repository findById(String id) {
		Optional<Repository> repo = repositoryRepository.findById(id);
		return repo.get();
	}
	
	public List<Repository> findAllByUserId(String userId) {
        return repositoryRepository.findByUserIdsIn(userId);
    }
	
	@Override
	public Repository findByIdAndUserId(String id, String userId) {
		return repositoryRepository.findByIdAndUsersIdsIn(id, userId);
	}

	@Override
	public Repository save(Repository repository) {
		return repositoryRepository.save(repository);
	}

	@Override
	public void delete(String id) {
		repositoryRepository.deleteById(id);
	}

	@Override
	public List<Repository> findByNameOrKeywords(String  regex) {
		return repositoryRepository.findByNameOrKeywordsRegex(formatSearchText(regex));
	}

	@Override
	public long count() {
		return repositoryRepository.count();
	}

	@Override
	public Repository findByName(String name) {
		return repositoryRepository.findByNameCaseInsensitive(formatSearchText(name));
	}
	
	/**
	 * Escape special characters
	 * @param input income text
	 * @return text with special characters escaped
	 */
	private String formatSearchText(String input) {
		String safeInput = input.replace("$", "\\$");
		safeInput = safeInput.replace("*", "\\*");
		safeInput = safeInput.replace(".", "\\.");
		safeInput = safeInput.replace("?", "\\?");
		return safeInput;
	}
	
}
