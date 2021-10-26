package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Stats;

public interface StatsDao {
	
	Stats save(Stats stats);
	Stats findByYearAndMonth(long year, long month);
	List<Stats> findByYear(long year);
	List<Stats> findAll();
	

}
