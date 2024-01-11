package fr.sedoo.certifymyrepo.rest.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;

@RunWith(SpringJUnit4ClassRunner.class)
public class OrcidDaoImplTest {
	
	@Mock
	RestTemplate restTemplate;
	
	@Test
	public void parseOrcidResponseWithEmail() throws IOException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("orcid-response-email-public.json").getFile());
		ResponseEntity<String> content = new ResponseEntity<>(FileUtils.readFileToString(file, Charset.defaultCharset()), HttpStatus.OK);
		when(restTemplate.getForEntity(any(), String.class)).thenReturn(content);
		
		OrcidDaoImpl orcidDao = new OrcidDaoImpl();
		orcidDao.setRestTemplate(restTemplate);
		ProfileDto user = orcidDao.getUserInfoByOrcid("0000-0000-0000-0000");
		assertEquals(user.getName(), "Toto Titi");
		assertEquals(user.getEmail(), "titi@gmail.com");
	}
	
	@Test
	public void parseOrcidResponseWithoutEmail() throws IOException {
		
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("orcid-response-email-private.json").getFile());
		ResponseEntity<String> content = new ResponseEntity<>(FileUtils.readFileToString(file, Charset.defaultCharset()), HttpStatus.OK);
		when(restTemplate.getForEntity(any(), String.class)).thenReturn(content);
		
		OrcidDaoImpl orcidDao = new OrcidDaoImpl();
		orcidDao.setRestTemplate(restTemplate);
		ProfileDto user = orcidDao.getUserInfoByOrcid("0000-0000-0000-0000");
		assertEquals(user.getName(), "Toto Titi");
		assertEquals(user.getEmail(), null);
	}

}
