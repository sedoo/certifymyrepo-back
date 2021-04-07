package fr.sedoo.certifymyrepo.rest.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.sedoo.certifymyrepo.rest.dto.UserLigth;
import fr.sedoo.certifymyrepo.rest.utils.JerseyClient;

@RunWith(SpringJUnit4ClassRunner.class)
public class OrcidDaoImplTest {
	
	@Mock
	JerseyClient client;
	
	@Test
	public void parseOrcidResponseWithEmail() throws IOException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("orcid-response-email-public.json").getFile());
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		when(client.getJsonResponse(any())).thenReturn(content);
		
		OrcidDao orcidDao = new OrcidDaoImpl(client);
		UserLigth user = orcidDao.getUserInfoByOrcid("0000-0000-0000-0000");
		assertEquals(user.getName(), "Toto Titi");
		assertEquals(user.getEmail(), "titi@gmail.com");
	}
	
	@Test
	public void parseOrcidResponseWithoutEmail() throws IOException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("orcid-response-email-private.json").getFile());
		String content = FileUtils.readFileToString(file, Charset.defaultCharset());
		when(client.getJsonResponse(any())).thenReturn(content);
		
		OrcidDao orcidDao = new OrcidDaoImpl(client);
		UserLigth user = orcidDao.getUserInfoByOrcid("0000-0000-0000-0000");
		assertEquals(user.getName(), "Toto Titi");
		assertEquals(user.getEmail(), null);
	}

}
