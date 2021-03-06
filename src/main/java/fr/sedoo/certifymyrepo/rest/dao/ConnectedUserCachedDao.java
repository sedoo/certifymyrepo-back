package fr.sedoo.certifymyrepo.rest.dao;

import java.util.ArrayList;
import java.util.List;
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
		String key = reportId.concat("%").concat(userId);
		cache.put(key, new ConnectedUser(userId, userName, userName.replaceAll("\\B.|\\P{L}", "").toUpperCase()));
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
