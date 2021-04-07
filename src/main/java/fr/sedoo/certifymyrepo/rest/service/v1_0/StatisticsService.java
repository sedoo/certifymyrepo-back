package fr.sedoo.certifymyrepo.rest.service.v1_0;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Statistics;

@RestController
@CrossOrigin
@RequestMapping(value = "/statistics/v1_0")
public class StatisticsService {

	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	RepositoryDao repositoryDao;
	
	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public Statistics statistics() {	
		return new Statistics(repositoryDao.count(), profileDao.count());
	}

}
