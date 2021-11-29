package fr.sedoo.certifymyrepo.rest.domain;


import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = Stats.STATS_COLLECTION_NAME, language = "english")
@TypeAlias("Stats")
public class Stats {

	public final static String STATS_COLLECTION_NAME = "stats";
	
	@Id
	private String id;
	private long year;
	private long month;
	private long users;
	private long repositories;
	private long greenReports;
	private long orangeReports;
	private long redReports;
	private long noReports;
	private long numberOfEditors;
	private long numberOfContributors;
	private long numberOfReaders;
	private long numberUsersWithoutRepo;
}
