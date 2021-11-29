package fr.sedoo.certifymyrepo.rest.service.statistics;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.dao.StatsDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.Stats;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryHealth;
import fr.sedoo.certifymyrepo.rest.dto.RoleCounter;
import fr.sedoo.certifymyrepo.rest.utils.RepositoryHealthCheck;

@Component
public class StatisticsProvider {
	
	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	RepositoryDao repositoryDao;
	
	@Autowired
	CertificationReportDao reportDao;
	
	@Autowired
	RepositoryHealthCheck repositoryHealthCheck;
	
	@Autowired
	StatsDao statsDao;
	
	@Scheduled(cron = "${statistics.cronExpression}")
	public Stats compute() {
		long currentYear = Calendar.getInstance().get(Calendar.YEAR);
		long currentMonth = Calendar.getInstance().get(Calendar.MONTH)+1;
		Stats stats = statsDao.findByYearAndMonth(currentYear, currentMonth);
		if(stats == null) {
			stats = new Stats();
			stats.setYear(currentYear);
			stats.setMonth(currentMonth);
		} else {
			stats.setNoReports(0);
			stats.setGreenReports(0);
			stats.setOrangeReports(0);
			stats.setRedReports(0);
		}
		
		RoleCounter rolesCounter = statsDao.countRoles();
		stats.setNumberOfContributors(rolesCounter.getNumberOfContributors());
		stats.setNumberOfReaders(rolesCounter.getNumberOfReaders());
		stats.setNumberOfEditors(rolesCounter.getNumberOfEditors());
		stats.setNumberUsersWithoutRepo(rolesCounter.getNumberUsersWithoutRepo());
		
		stats.setUsers(profileDao.count());
		List<Repository> repos = repositoryDao.findAll();
		if(repos != null) {
			stats.setRepositories(repos.size());
			for(Repository repo: repos) {
				RepositoryHealth health =null;
				CertificationReport validated = reportDao.findReportValidatedByRepositoryIdAndMaxUpdateDate(repo.getId());
				CertificationReport inProgress = reportDao.findReportInProgressByRepositoryIdAndMaxUpdateDate(repo.getId());
				if(validated != null && inProgress == null) {
					health = repositoryHealthCheck.compute(validated);
				} else if(validated == null && inProgress != null) {
					health = repositoryHealthCheck.compute(inProgress);
				} else if(validated != null && inProgress != null) {
					if(validated.getUpdateDate().before(inProgress.getUpdateDate())) {
						health = repositoryHealthCheck.compute(inProgress);
					} else {
						health = repositoryHealthCheck.compute(validated);
					}
				} else {
					stats.setNoReports(stats.getNoReports()+1);
					continue;
				}
				if(health.isGreen()) {
					stats.setGreenReports(stats.getGreenReports()+1);
				} else if(health.isOrange()) {
					stats.setOrangeReports(stats.getOrangeReports()+1);
				} else if(health.isRed()) {
					stats.setRedReports(stats.getRedReports()+1);
				}
			}
		}
		return statsDao.save(stats);
	}

}
