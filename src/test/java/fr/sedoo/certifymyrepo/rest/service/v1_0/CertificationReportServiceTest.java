package fr.sedoo.certifymyrepo.rest.service.v1_0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.dao.AttachmentDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.dao.CommentsDao;
import fr.sedoo.certifymyrepo.rest.dao.ConnectedUserDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.MyReport;
import fr.sedoo.certifymyrepo.rest.domain.MyReports;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.LevelTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.ForbiddenException;

@RunWith(MockitoJUnitRunner.class)
public class CertificationReportServiceTest {
	
	@Mock
	RepositoryDao repositoryDaoMock;
	
	@Mock
	CertificationReportDao certificationReportDaoMock;
	
	@Mock
	CommentsDao commentsDaoMock;
	
	@Mock
	CertificationReportTemplateDao certificationReportTemplateMock;
	
	@Mock
	AttachmentDao ftpClient;
	
	@Mock
	ApplicationUser applicationUser;
	@Mock
	Authentication authentication;
	@Mock
	SecurityContext securityContext;
	
	@Mock
	ConnectedUserDao connectedUserDao;
	
	@InjectMocks
	private CertificationReportService reportService;
	
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
        ApplicationUser user = new ApplicationUser("1234","Mister Test", authorities);
        when(authentication.getPrincipal()).thenReturn(user);
        
    }
	
	@Test
    public void testListAllRepository() {
		initiliaziseRepositoryWithReader(); 
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		List<CertificationItem> items = new ArrayList<>();
		report.setItems(items);
		List<CertificationReport> reports = Arrays.asList(new CertificationReport[] {report});
		when(certificationReportDaoMock.findByRepositoryId("repository_id")).thenReturn(reports);
		CertificationTemplate value = new CertificationTemplate();
		List<LevelTemplate> list = new ArrayList<LevelTemplate>();
		value.setLevels(list);
		List<RequirementTemplate> requirements = new ArrayList<RequirementTemplate>();
		value.setRequirements(requirements);
		when(certificationReportTemplateMock.getCertificationReportTemplate(any())).thenReturn(value);
		
		MyReports result = reportService.listByRepositoryId("myToken", "repository_id");
        assertTrue(result.getReports().size() == 1);
        assertEquals(result.getReports().get(0).getVersion(), "1.1");
	}
	
	@Test
    public void testListAllRepositoryForbidenAccess() {
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		try {
			reportService.listByRepositoryId("myToken", "123");
			assertTrue("An exception had to be thrown", false);
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
		}
	}
	
	@Test
    public void testgetReportUserReader() {
		initiliaziseRepositoryWithReader();
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		when(certificationReportDaoMock.findById("report_id")).thenReturn(report);
		
		try {
			MyReport result = reportService.getReport("myToken", "report_id");
	        assertNotNull(result.getReport());
	        assertFalse("Must be in READ ONLY mode",result.isEditExistingAllowed());
	        assertFalse("Must be in READ ONLY mode",result.isValidationAllowed());
		} catch(ForbiddenException e) {
			assertTrue("No exception had to be thrown", false);
		}
	}
	
	@Test
    public void testGetReportUserManager() {
		initiliaziseRepositoryWithEditor();
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		when(certificationReportDaoMock.findById("report_id")).thenReturn(report);
		
		try {
			MyReport result = reportService.getReport("myToken", "report_id");
			assertNotNull(result.getReport());
	        assertTrue("An editor can edit an existing report",result.isEditExistingAllowed());
	        assertTrue("An editor can validate a report",result.isValidationAllowed());
		} catch(ForbiddenException e) {
			assertTrue("No exception had to be thrown", false);
		}
	}
	
	@Test
    public void testgetReportForbidenAccess() {
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		when(certificationReportDaoMock.findById("report_id")).thenReturn(report);
		
		try {
			reportService.getReport("myToken", "report_id");
			assertTrue("An exception had to be thrown", false);
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
		}
	}
	
	@Test
    public void testSaveAccessForbidenAccessNoUser() {
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		
		try {
			reportService.saveJson("myToken", report, "fr");
			assertTrue("An exception had to be thrown", false);
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
		}
	}
	
	@Test
    public void testSaveAccessForbidenAccessUserReader() {
		initiliaziseRepositoryWithReader();
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		
		try {
			reportService.saveJson("myToken", report, "fr");
			assertTrue("An exception had to be thrown", false);
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
		}
	}	
	
	@Test
    public void testSaveUserManager() {
		initiliaziseRepositoryWithEditor();
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		
		when(certificationReportDaoMock.save(report)).thenReturn(report);
		
		try {
			CertificationReport result = reportService.saveJson("myToken", report, "fr");
			assertEquals(report.getVersion(),result.getVersion());
		} catch(ForbiddenException e) {
			assertTrue("An exception had to be thrown", false);
		}
	}	

	@Test
    public void testDeleteAccessForbidenAccessNoUser() {
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		report.setStatus(ReportStatus.NEW);
		when(certificationReportDaoMock.findById("report_id")).thenReturn(report);
		
		try {
			reportService.delete("myToken", "report_id");
			assertTrue("An exception had to be thrown", false);
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
		}
	}	
	
	@Test
    public void testDeleteAccessForbidenAccessUserReader() {
		initiliaziseRepositoryWithReader();
		CertificationReport report = new CertificationReport();
		report.setVersion("1.1");
		report.setId("report_id");
		report.setRepositoryId("repository_id");
		report.setStatus(ReportStatus.NEW);
		when(certificationReportDaoMock.findById("report_id")).thenReturn(report);
		
		try {
			reportService.delete("myToken", "report_id");
			assertTrue("An exception had to be thrown", false);
		} catch(ResponseStatusException e) {
			assertEquals("Precondition Failed", e.getStatus().getReasonPhrase());
		}
	}
	
	private void initiliaziseRepositoryWithReader() {
		// Orcid 0000-0000-0000-1234 is READER of the repo
		Repository repo = new Repository();
		RepositoryUser user = new RepositoryUser("1234", Roles.READER);
		repo.setUsers(Arrays.asList(new RepositoryUser[] {user}));
		when(repositoryDaoMock.findByIdAndUserId("repository_id", "1234")).thenReturn(repo);
	}
	
	private void initiliaziseRepositoryWithEditor() {
		// Orcid 0000-0000-0000-1234 is READER of the repo
		Repository repo = new Repository();
		RepositoryUser user = new RepositoryUser("1234", Roles.EDITOR);
		repo.setUsers(Arrays.asList(new RepositoryUser[] {user}));
		when(repositoryDaoMock.findByIdAndUserId("repository_id", "1234")).thenReturn(repo);
	}

}
