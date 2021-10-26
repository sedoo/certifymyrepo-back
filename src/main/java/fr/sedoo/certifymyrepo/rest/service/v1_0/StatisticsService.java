package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.dao.StatsDao;
import fr.sedoo.certifymyrepo.rest.domain.Stats;
import fr.sedoo.certifymyrepo.rest.domain.YearllyStatsDto;
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
	
	@RequestMapping(value = "/getStats", method = RequestMethod.GET)
	public List<YearllyStatsDto> getStats() {	
		List<YearllyStatsDto> list = new ArrayList<YearllyStatsDto>();
		Map<Long, List<Stats>> map = this.getStatsByYear();
		
		for (Map.Entry<Long, List<Stats>> entry : map.entrySet()) {
			
			// get the list of data for the first semester
			List<Stats> firstSemesterData = entry.getValue().stream()
					.filter(c -> c.getMonth() >= 1 && c.getMonth() <= 6)
					.collect(Collectors.toList());
			
			// if the semester has data find the highest month
			Stats maxMonthInFirstSemester = null;
			if(firstSemesterData != null && firstSemesterData.size() > 0) {
				maxMonthInFirstSemester = firstSemesterData.stream()
						.filter(c -> c.getMonth() >= 1 && c.getMonth() <= 6)
						.max(Comparator.comparing(Stats::getMonth))
						.get();
			}
			

			
			// get the list of data for the first semester
			List<Stats> secondSemesterData = entry.getValue().stream()
					.filter(c -> c.getMonth() >= 7 && c.getMonth() <= 12)
					.collect(Collectors.toList());
			
			// if the semester has data find the highest month
			Stats maxMonthInSecondSemester = null;
			if(secondSemesterData != null && secondSemesterData.size() > 0) {
				maxMonthInSecondSemester = secondSemesterData.stream()
						.filter(c -> c.getMonth() >= 7 && c.getMonth() <= 12)
						.max(Comparator.comparing(Stats::getMonth))
						.get();
			}
			
			YearllyStatsDto yearllyStatsDto = new YearllyStatsDto();
			yearllyStatsDto.setYear(entry.getKey());
			yearllyStatsDto.setFirstSemester(maxMonthInFirstSemester);
			yearllyStatsDto.setSecondSemester(maxMonthInSecondSemester);
			list.add(yearllyStatsDto);
		}
		
		return list;
	}
	
	private Map<Long, List<Stats>> getStatsByYear() {
		Map<Long, List<Stats>> map = new HashMap<Long, List<Stats>>();
		List<Stats> stats = statsDao.findAll();
		for(Stats stat : stats) {
			if(map.containsKey(stat.getYear())) {
				List<Stats> list = map.get(stat.getYear());
				list.add(stat);
				map.put(stat.getYear(), list);
			} else {
				List<Stats> list = new ArrayList<Stats>();
				list.add(stat);
				map.put(stat.getYear(), list);
			}
		}
		return map;
	}
}
