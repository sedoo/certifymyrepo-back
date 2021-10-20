package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import fr.sedoo.certifymyrepo.rest.domain.Stats;

public interface StatsRepository extends MongoRepository<Stats, String> {
	
	Stats findByYearAndMonth(long year, long month);
	List<Stats> findByYear(long year);

}
