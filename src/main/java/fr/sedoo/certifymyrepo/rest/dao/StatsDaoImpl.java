package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Stats;

@Component
public class StatsDaoImpl implements StatsDao {
	
	@Autowired
	private StatsRepository repository;

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


}
