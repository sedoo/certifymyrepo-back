package fr.sedoo.certifymyrepo.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import fr.sedoo.IntegrationTest;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.ftp.SimpleFtpClient;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
@Category(IntegrationTest.class)
public class ApplicationIntegrationTest {
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private AdminDao adminDao;
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	@Autowired
	private CertificationReportDao reportDao;
	
	@Autowired
	private SimpleFtpClient ftpClient;
	
	@Value("classpath:Test.txt")
	Resource testFile;

	@Test
	public void daoLayerTest() {	
		List<Admin> admins = adminDao.findAllSuperAdmin();
		assertEquals("super admin has been declared in application-local.yml SUPER_ADMIN_ORCID_LIST: xxx,yyy", 2, admins.size());
		
		Optional<Profile> userProfile = profileDao.findById(admins.get(0).getUserId());
		assertTrue("A profile must exist for this amdin", userProfile.isPresent());
		
		// Given a repository named junit-integration 
		// with one User 111-222-333
		Repository repository = new Repository();
		repository.setName("junit-integration");
		List<RepositoryUser> users = new ArrayList<RepositoryUser>();
		RepositoryUser user = new RepositoryUser();
		user.setId("111-222-333");
		user.setRole(Roles.EDITOR);
		users.add(user);
		repository.setUsers(users);
		repository = repositoryDao.save(repository);
		
		// Check repositories queries
		assertEquals(1,repositoryDao.findByNameOrKeywords("junit-i").size());
		assertNotNull(repositoryDao.findByName("junit-integration"));
		assertNotNull(repositoryDao.findAllByUserId("111-222-333"));
		
		// Given two report on 'junit-integration' repository 
		// - released report version 0.0
		// - report in progress version 0.1
		CertificationReport certificationReport = new CertificationReport();
		certificationReport.setVersion("0.0");
		certificationReport.setRepositoryId(repository.getId());
		certificationReport.setStatus(ReportStatus.RELEASED);
		reportDao.save(certificationReport);
		
		certificationReport = new CertificationReport();
		certificationReport.setVersion("0.1");
		certificationReport.setRepositoryId(repository.getId());
		certificationReport.setStatus(ReportStatus.IN_PROGRESS);
		reportDao.save(certificationReport);
		
		// Check reports queries
		CertificationReport inprogress = reportDao.findReportInProgressByRepositoryIdAndMaxUpdateDate(repository.getId());
		assertEquals("0.1", inprogress.getVersion());
		
		CertificationReport validated = reportDao.findReportValidatedByRepositoryIdAndMaxUpdateDate(repository.getId());
		assertEquals("0.0", validated.getVersion());

		// Clean data base
		reportDao.delete(validated.getId());
		reportDao.delete(inprogress.getId());
		repositoryDao.delete(repository.getId());
	}
	
	@Test
	public void ftpTest() throws IOException {	
		ftpClient.uploadFile(testFile.getInputStream(), "junit/0", testFile.getFilename());
		assertTrue(ftpClient.checkDirectoryExistance("junit"));
		assertEquals("Test.txt", ftpClient.listFiles("junit").get("0").get(0));
		assertTrue(ftpClient.deleteFile("junit/0", "Test.txt"));
		assertEquals(0, ftpClient.listFiles("junit").get("0").size());
		assertTrue(ftpClient.deleteFolder("junit/0"));
		assertTrue(ftpClient.deleteFolder("junit"));
		assertFalse(ftpClient.checkDirectoryExistance("junit"));
	}

}
