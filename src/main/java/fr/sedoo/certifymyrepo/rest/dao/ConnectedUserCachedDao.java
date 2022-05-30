package fr.sedoo.certifymyrepo.rest.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.sedoo.certifymyrepo.rest.domain.ConnectedUser;

@Component
public class ConnectedUserCachedDao implements ConnectedUserDao {
	
	Cache<String, ConnectedUser> cache = CacheBuilder.newBuilder().maximumSize(100)
			.expireAfterWrite(45, TimeUnit.SECONDS).build();

	@Override
	public void updateCache(String reportId, String userId, String userName) {
		boolean isReadOnly = false;
		List<ConnectedUser> connectedUsers = getConnectedUsersByReportId(reportId);
		Date creationDate = new Date();
		if(connectedUsers.size() > 0) {
			// Get from the list in cache the user currently connected
			Optional<ConnectedUser> user = connectedUsers.stream().filter(p -> StringUtils.equals(p.getUserId(), userId)).findFirst();
			if(user.isPresent()) {
				creationDate = user.get().getCreationDate();
				// If the currently connected user has read only awaiting for write access, need to check if he is first in the waiting list
				if(user.get().isReadOnly()) {
					Collections.sort(connectedUsers);
					if(connectedUsers.get(0).getCreationDate().compareTo(creationDate) != 0) {
						isReadOnly = true;
					};
				}
			} else {
				// if the list contains already an or some users, the current user has to get read only access.
				isReadOnly = true;
			}
		}
		String key = reportId.concat("%").concat(userId);
		cache.put(key, new ConnectedUser(userId, userName, userName.replaceAll("\\B.|\\P{L}", "").toUpperCase(), isReadOnly, creationDate));
	}

	@Override
	public List<ConnectedUser> getConnectedUsersByReportId(String reportId) {
		List<ConnectedUser> connectedUsers = new ArrayList<ConnectedUser>();
		for(String key : cache.asMap().keySet()) {
			String cachedReportId = key.split("%")[0];
			if(StringUtils.equals(cachedReportId, reportId)) {
				connectedUsers.add(cache.getIfPresent(key));
			}
		}
		return connectedUsers;
	}

}
