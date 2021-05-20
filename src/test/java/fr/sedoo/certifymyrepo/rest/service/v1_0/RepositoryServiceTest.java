package fr.sedoo.certifymyrepo.rest.service.v1_0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import fr.sedoo.certifymyrepo.rest.dao.AffiliationDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.LevelTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.dto.FullRepositoryDto;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryUserDto;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.notification.NotificationService;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.BadRequestException;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.ForbiddenException;

@RunWith(MockitoJUnitRunner.class)
public class RepositoryServiceTest {
	
	@Mock
	RepositoryDao repositoryDaoMock;
	
	@Mock
	CertificationReportDao certificationReportDaoMock;
	
	@Mock
	private ApplicationUser applicationUser;
	
	@Mock
	private Authentication authentication;
	
	@Mock
	private SecurityContext securityContext;
	
	@Mock
	private CertificationReportTemplateDao templateDao;
	
	@Mock
	private NotificationService notificationService;
	
	@Mock
	private AffiliationDao affiliationDao;
	
	@InjectMocks
	private RepositoryService repositoryService;
	
	@Before
	public void initMock() {
		// security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(applicationUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        
        // Authenticated user has an USER role
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(Roles.AUTHORITY_USER));
        ApplicationUser user = new ApplicationUser("0000-0000-0000-1234","Mister Test", authorities);
        when(authentication.getPrincipal()).thenReturn(user);
        
        // mock repository
        when(repositoryDaoMock.findAllByUserId("0000-0000-0000-1234")).thenReturn(createRepositoryList());
        
	}
	
	private List<Repository> createRepositoryList() {
        Repository repo = new Repository();
        repo.setId("123");
        repo.setName("AERIS");
		RepositoryUser user = new RepositoryUser();
		user.setId("0000-0000-0000-1234");
		user.setRole("READER");
		repo.setUsers(Arrays.asList(new RepositoryUser[] {user}));
        Repository repo2 = new Repository();
        repo2.setId("456");
        repo2.setName("PIC DE NORE");
		RepositoryUser user2 = new RepositoryUser();
		user2.setId("0000-0000-0000-1234");
		user2.setRole("READER");
		repo2.setUsers(Arrays.asList(new RepositoryUser[] {user2}));
        return Arrays.asList(new Repository[]{repo, repo2});
	}
	
	private CertificationReport createCertificationReport(String level) {
		CertificationReport report = new CertificationReport();
		report.setId("report_1");
		report.setVersion("1.0");
		report.setRepositoryId("123");
		report.setStatus(ReportStatus.RELEASED);
		report.setUpdateDate(new Date());
		CertificationItem item1 = new CertificationItem();
		item1.setCode("R0");
		item1.setLevel(level);
		CertificationItem item2 = new CertificationItem();
		item2.setCode("R1");
		item2.setLevel(level);
		CertificationItem item3 = new CertificationItem();
		item3.setCode("R2");
		item3.setLevel(level);
		List<CertificationItem> items = Arrays.asList(new CertificationItem[] {item1, item2, item3});
		report.setItems(items);
		return report;
	}
	
	@Test
    public void testListAllFullRepositoryGreenHealthCheck() {
		
        when(certificationReportDaoMock.findReportInProgressByRepositoryIdAndMaxUpdateDate(anyString())).thenReturn(createCertificationReport("4"));
		CertificationTemplate value = new CertificationTemplate();
		List<LevelTemplate> list = new ArrayList<LevelTemplate>();
		value.setLevels(list);
		List<RequirementTemplate> requirements = new ArrayList<RequirementTemplate>();
		value.setRequirements(requirements);
		when(templateDao.getCertificationReportTemplate(any())).thenReturn(value);
		
        List<FullRepositoryDto> result = repositoryService.listAllFullRepositories("myToken");
        
        assertTrue(result.size() == 2);
        assertEquals(result.get(0).getRepository().getName(), "AERIS");
        assertTrue(result.get(0).getHealthLatestInProgressReport().isGreen());
	}
	
	@Test
    public void testListAllFullRepositoryRedHealthCheck() {
		
        when(certificationReportDaoMock.findReportInProgressByRepositoryIdAndMaxUpdateDate(anyString())).thenReturn(createCertificationReport("1"));
		CertificationTemplate value = new CertificationTemplate();
		List<LevelTemplate> list = new ArrayList<LevelTemplate>();
		value.setLevels(list);
		List<RequirementTemplate> requirements = new ArrayList<RequirementTemplate>();
		value.setRequirements(requirements);
		when(templateDao.getCertificationReportTemplate(any())).thenReturn(value);
		
        List<FullRepositoryDto> result = repositoryService.listAllFullRepositories("myToken");
        
        assertTrue(result.size() == 2);
        assertEquals(result.get(0).getRepository().getName(), "AERIS");
        assertTrue(result.get(0).getHealthLatestInProgressReport().isRed());
	}
	
	@Test
    public void testSearchOneKeyword() {
		
		Repository repo1 = new Repository();
		repo1.setName("REPO_1");
		List<Repository> list = Arrays.asList(new Repository[] {repo1});
		when(repositoryDaoMock.findByNameOrKeywords("toto")).thenReturn(list);
		
        List<Repository> result = repositoryService.search("my_token", Collections.singletonList("toto"));
        
        assertEquals(repo1.getName(),  result.get(0).getName());
	}
	
	@Test
    public void testSearchSeveralKeywords() {
		
		Repository repo1 = new Repository();
		repo1.setName("REPO_1");
		List<Repository> list = Arrays.asList(new Repository[] {repo1});
		when(repositoryDaoMock.findByNameOrKeywords("toto|tintin")).thenReturn(list);
		
        List<Repository> result = repositoryService.search("my_token", Arrays.asList(new String[] {"toto", "tintin"}));
        
        assertEquals(repo1.getName(),  result.get(0).getName());
	}
	
	@Test
    public void testSaveEmptyParameter() {
		try {
			repositoryService.save("myToken", null, "fr");
			assertTrue("An exception had to be thrown", false);
		} catch (BadRequestException e) {
			assertTrue(true);
		}
	}
	
	@Test
    public void testSaveWithNotification() {
		try {
			
			// simulate an updated repository coming from the front-end
			Repository repoToSave = createRepositoryToSave();
			
			Repository existingRepo = createRepositoryAleadyInDB();
			// simulate an existing repository in DB
			when(repositoryDaoMock.findById("1")).thenReturn(existingRepo);
			// used for the duplicate name check
			when(repositoryDaoMock.findByName("SSS")).thenReturn(existingRepo);
			
			// Allow us to check if the code reach the notification point
			// As when an user is remove or added an email must be sent
			when(notificationService.sendNotification(anySet(), anyString(), anyString(), any(), anyList()))
				.thenThrow(new RuntimeException("It passed by here"));

			Repository result = repositoryService.save("myToken", repoToSave, "fr");
			assertTrue("The notification have not been reached", result != null);

		} catch (Exception e) {
			if(StringUtils.equals(e.getMessage(), "It passed by here")) {
				assertTrue("The notification have been reached", true);
			} else {
				assertTrue("Exception should not o be thrown", false);
			}

		}
	}
	
	@Test
    public void testDeleteForbidenAcces() {
		try {
			repositoryService.delete("myToken", "789");
			assertTrue("An exception had to be thrown", false);
		} catch (ForbiddenException e) {
			assertTrue(true);
		}
	}
	
	@Test
    public void testDelete() {
		when(repositoryDaoMock.findByIdAndUserId("456", "0000-0000-0000-1234")).thenReturn(new Repository());
		try {
			repositoryService.delete("myToken", "456");
			assertTrue("No exception", true);
		} catch (ForbiddenException e) {
			assertTrue("No exception had to be thrown", false);
		}
	}
	
	/**
	 * A repository with 3 users to simulate an updated repository coming from the front-end
	 * Two user has been deleted
	 * One has been added
	 * @return {@link Repository}
	 */
	private Repository createRepositoryToSave() {
		Repository repo = new Repository();
		repo.setId("1");
		repo.setName("SSS");
		RepositoryUser user1 = new RepositoryUser();
		user1.setId("123");
		user1.setRole(Roles.EDITOR);
		RepositoryUser user2 = new RepositoryUser();
		user2.setId("654");
		user2.setRole(Roles.EDITOR);
		RepositoryUser user3 = new RepositoryUser();
		user3.setId("789");
		user3.setRole(Roles.READER);
		repo.setUsers(Arrays.asList(new RepositoryUser[] {user1, user2, user3}));
		return repo;
	}
	
	/**
	 * A repository with 4 users to simulate an existing repository in DB
	 * @return {@link Repository}
	 */
	private Repository createRepositoryAleadyInDB() {
		Repository repo = new Repository();
		repo.setId("1");
		repo.setName("SSS");
		RepositoryUser user1 = new RepositoryUser();
		user1.setId("123");
		user1.setRole(Roles.EDITOR);
		RepositoryUser user2 = new RepositoryUser();
		user2.setId("456");
		user2.setRole(Roles.EDITOR);
		RepositoryUser user3 = new RepositoryUser();
		user3.setId("789");
		user3.setRole(Roles.READER);
		RepositoryUser user4 = new RepositoryUser();
		user4.setId("910");
		user4.setRole(Roles.READER);
		repo.setUsers(Arrays.asList(new RepositoryUser[] {user1, user2, user3, user4}));
		return repo;
	}

}
