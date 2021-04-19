package fr.sedoo.certifymyrepo.rest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
//@TestPropertySource(properties = {"CAMEL_LOG_FILE=."})
public class CertifyMyRepoRestApplicationTest {
	
	@Autowired
	private ProfileDao ProfileDao;

	@Test
	public void contextLoads() {
		assertNotNull(ProfileDao);
	}

}
