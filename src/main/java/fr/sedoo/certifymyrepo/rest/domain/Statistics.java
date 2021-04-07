package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Statistics {
	
	public Statistics(long repositories, long users) {
		this.setRepositories(repositories);
		this.setUsers(users);
	}
	
	long repositories;
	long users;

}
