package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.dao.StatsDao;
import fr.sedoo.certifymyrepo.rest.domain.Stats;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.statistics.StatisticsProvider;

@RestController
@CrossOrigin
@RequestMapping(value = "/statistics/v1_0")
public class StatisticsService {
	
	@Autowired
	StatisticsProvider statisticsProvider;
	
	@Autowired
	StatsDao statsDao;
	
	//@Secured({Roles.AUTHORITY_ADMIN})
	@RequestMapping(value = "/computedStats", method = RequestMethod.GET)
	public Stats computedStats() {	
		return statisticsProvider.compute();
	}
	
	@Secured({Roles.AUTHORITY_ADMIN})
	@RequestMapping(value = "/getAnnualStats", method = RequestMethod.GET)
	public List<Stats> getAnnualStats(@RequestHeader("Authorization") String authHeader, long year) {	
		return statsDao.findByYear(year);
	}

}
