package fr.sedoo.certifymyrepo.rest.service.v1_0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import fr.sedoo.certifymyrepo.rest.dao.ConnectedUserCachedDao;
import fr.sedoo.certifymyrepo.rest.domain.ConnectedUser;

public class ConnectedUserCachedDaoTest {
	
	@Test
	public void test() {
		ConnectedUserCachedDao dao = new ConnectedUserCachedDao();
		dao.updateCache("1111", "1111", "Jean Claude Convenant");
		dao.updateCache("1111", "2222", "Hervé Dumont");
		
		List<ConnectedUser> users = dao.getConnectedUsersByReportId("1111");
		
		List<String> names = users.stream()
				   .map(object -> object.getShortName()).sorted()
				   .collect(Collectors.toList());
		
        assertTrue(users.size() == 2);
        assertEquals(names.get(0), "HD");
        assertEquals(names.get(1), "JCC");
	}

}
