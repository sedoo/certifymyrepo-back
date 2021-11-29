package fr.sedoo.certifymyrepo.rest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleCounter {

	private long numberOfEditors;
	private long numberOfContributors;
	private long numberOfReaders;
	private long numberUsersWithoutRepo;
	
}
