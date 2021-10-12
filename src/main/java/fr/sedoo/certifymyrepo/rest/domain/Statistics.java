package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Statistics {
	
	public Statistics(long repositories, long users, long inProgress, long validated) {
		this.setRepositories(repositories);
		this.setUsers(users);
		this.setInProgress(inProgress);
		this.setValidated(validated);
	}
	
	long repositories;
	long users;
	long inProgress;
	long validated;

}
