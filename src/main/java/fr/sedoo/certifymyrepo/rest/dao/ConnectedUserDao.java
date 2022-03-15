package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.ConnectedUser;

public interface ConnectedUserDao {
	
	void updateCache(String reportId, String userId, String userName);
	List<ConnectedUser> getConnectedUsersByReportId(String reportId);

}
