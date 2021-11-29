package fr.sedoo.certifymyrepo.rest.dao;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.Stats;
import fr.sedoo.certifymyrepo.rest.dto.RoleCounter;

@Component
public class StatsDaoImpl implements StatsDao {
	
	@Autowired
	private StatsRepository repository;
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public Stats save(Stats stats) {
		return repository.save(stats);
	}

	@Override
	public Stats findByYearAndMonth(long year, long month) {
		return repository.findByYearAndMonth(year, month);
	}

	@Override
	public List<Stats> findByYear(long year) {
		return repository.findByYear(year);
	}

	@Override
	public List<Stats> findAll() {
		return repository.findAll();
	}

	@Override
	public RoleCounter countRoles() {
		RoleCounter roleCoumpter = new RoleCounter();
		Aggregation aggregation = newAggregation(
			      unwind("$users"), group("users.role").count().as("number")
			  );
		AggregationResults<Map> result = mongoTemplate.aggregate(aggregation,  Repository.class, Map.class);
		if(result != null && ! result.getMappedResults().isEmpty()) {
			ListIterator<Map> it = result.getMappedResults().listIterator();
			while(it.hasNext()) {
				Map item = it.next();
				if(item.get("_id").equals("EDITOR")) {
					roleCoumpter.setNumberOfEditors( new Long((int) item.get("number")));
				} else if(item.get("_id").equals("CONTRIBUTOR")) {
					roleCoumpter.setNumberOfContributors( new Long((int) item.get("number")));
				} else if(item.get("_id").equals("READER")) {
					roleCoumpter.setNumberOfReaders( new Long((int) item.get("number")));
				}
			}
		}
		
		List<Profile> users = profileDao.findAll();
		if(users != null) {
			List<String> usersIdList = users.stream().map(Profile::getId).collect(Collectors.toList());
			long usersWithoutRepo = 0;
			for(String userId : usersIdList) {
				List<Repository> reports = this.repositoryDao.findAllByUserId(userId);
				if(reports == null || reports.isEmpty()) {
					usersWithoutRepo++;
				}
			}
			roleCoumpter.setNumberUsersWithoutRepo(usersWithoutRepo);
		}
		return roleCoumpter;
	}


}
