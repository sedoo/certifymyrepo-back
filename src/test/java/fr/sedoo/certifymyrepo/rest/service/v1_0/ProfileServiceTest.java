package fr.sedoo.certifymyrepo.rest.service.v1_0;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;

@RunWith(MockitoJUnitRunner.class)
public class ProfileServiceTest {
	
	@Mock
	private ProfileDao profileDao;
	
	@Mock
	private RepositoryDao repositoryDao;
	
	@InjectMocks
	private ProfileService profileService;
	
	@Test
    public void testDuplicateEmail() {
		try {
			Profile profile = new Profile();
			profile.setName("TEST");
			profile.setEmail("toto@gmail.com");
			
			Profile existingProfile = new Profile();
			existingProfile.setName("TOTO");
			existingProfile.setEmail("toto@gmail.com");
			
			when(profileDao.findByEmail("toto@gmail.com")).thenReturn(existingProfile);
			profileService.createNewProfile("myToken", profile, "fr");
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
			assertEquals("Ce courriel est déjà dans la base de données pour l'utilisateur TOTO", e.getReason());
		}
	}
	
	@Test
    public void testDuplicateOrcid() {
		try {
			Profile profile = new Profile();
			profile.setName("TEST");
			profile.setEmail("toto@gmail.com");
			profile.setOrcid("111-222-333");
			
			when(profileDao.findByOrcid("111-222-333")).thenReturn(new Profile());
			profileService.createNewProfile("myToken", profile, "fr");
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
			assertEquals("Cet ORCID est déjà dans la base de données pour l'utilisateur TEST", e.getReason());
		}
	}
	
	@Test
	public void testDeleteUser() {
		List<Repository> repos = new ArrayList<Repository>();
		when(repositoryDao.findAllByUserId("123-123-123")).thenReturn(repos);
		String result = profileService.deleteProfileSimulation("myToken", "fr", "123-123-123");
		assertEquals("", result);
		
		Repository repo1 = new Repository();
		repo1.setName("My first repo");
		List<RepositoryUser> users = new ArrayList<RepositoryUser>();
		users.add(new RepositoryUser("123-123-123", Roles.EDITOR));
		users.add(new RepositoryUser("789-789-789", Roles.READER));
		repo1.setUsers(users);
		
		Repository repo2 = new Repository();
		repo2.setName("My second repo");
		users = new ArrayList<RepositoryUser>();
		users.add(new RepositoryUser("123-123-123", Roles.READER));
		users.add(new RepositoryUser("789-789-789", Roles.EDITOR));
		repo2.setUsers(users);
		
		repos.add(repo1);
		repos.add(repo2);
		when(repositoryDao.findAllByUserId("123-123-123")).thenReturn(repos);
		result = profileService.deleteProfileSimulation("myToken", "fr", "123-123-123");
		assertEquals("Vous allez être supprimé des entrepôts: My second repo<br/>Les entrepôts suivant vont êtres supprimés: My first repo<br/>", result);
	}

}
