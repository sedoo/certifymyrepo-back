package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.config.MailConfig;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.dto.UserLigth;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;
import fr.sedoo.certifymyrepo.rest.service.notification.EmailSender;

@RestController
@CrossOrigin
@RequestMapping(value = "/profile/v1_0")
public class ProfileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	AdminDao adminDao;
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	@Autowired
	private CertificationReportDao certificationReportDao;
	
	@Autowired
	private EmailSender emailSender;
	
	@Autowired
	private MailConfig mailConfig;
	
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public Profile profile(@RequestHeader("Authorization") String authHeader) {	
		return profileDao.findById(LoginUtils.getLoggedUser().getUserId());
    }
	
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/saveProfile", method = RequestMethod.POST)
    public Profile saveProfile(@RequestHeader("Authorization") String authHeader, @RequestBody Profile profile) {
		return profileDao.save(profile);
    }
	
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/createNewProfile", method = RequestMethod.POST)
    public Profile createNewProfile(@RequestHeader("Authorization") String authHeader, 
    		@RequestBody Profile profile,
    		@RequestParam String language) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        
		if(profile.getEmail() != null && profileDao.findByEmail(profile.getEmail()) != null) {
			throw new BusinessException(messages.getString("create.user.error.duplicate.orcid").concat(profile.getName()));
		}
		if(profile.getOrcid() != null && profileDao.findByOrcid(profile.getOrcid()) != null) {
			throw new BusinessException(messages.getString("create.user.error.duplicate.email").concat(profile.getName()));
		}
		Profile createdProfile = profileDao.save(profile);
		
		if(createdProfile.getEmail() != null) {
			List<String> superAdminEmails = new ArrayList<String>();
			List<Admin> superAdmins = adminDao.findAllSuperAdmin();
			for(Admin superAdmin : superAdmins) {
				Profile userProfile = profileDao.findById(superAdmin.getUserId());
				superAdminEmails.add(userProfile.getEmail());
			}
			
			try {
				SimpleEmail simpleemail = new SimpleEmail();
				simpleemail.setHostName(mailConfig.getHostname());
				simpleemail.addTo(createdProfile.getEmail());
				simpleemail.addCc(superAdminEmails.toArray(new String[superAdminEmails.size()]));
				simpleemail.setFrom(mailConfig.getFrom());
				simpleemail.setSubject(messages.getString("create.user.notification.subject"));
				String content = String.format(messages.getString("create.user.notification.orcid.content"), profile.getOrcid());
				emailSender.send(simpleemail, content);
			} catch (AddressException | EmailException e) {
				LOG.error("Notification could not be send to ".concat(createdProfile.getEmail()), e);;
			}
		}
		return createdProfile;
    }
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/listAllUsers", method = RequestMethod.GET)
	public List<UserLigth> listAll(@RequestHeader("Authorization") String authHeader) {
		List<UserLigth> result = new ArrayList<>();
		List<Profile> usersProfile = profileDao.findAll();
		if(usersProfile != null) {
			for( Profile userProfile : usersProfile) {
				UserLigth user = new UserLigth();
				user.setUserId(userProfile.getId());
				user.setEmail(userProfile.getEmail());
				user.setName(userProfile.getName());
				result.add(user);
			}
		}
		return result;
	}
	
	/**
	 */
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/deleteProfileSimulation/{language}/{id}", method = RequestMethod.GET)
    public String deleteProfileSimulation(@RequestHeader("Authorization") String authHeader, 
    		@PathVariable(name = "language") String language,
    		@PathVariable(name = "id") String id) {
		
		return delete(language, id, true);
	}
	
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/deleteProfile/{language}/{id}", method = RequestMethod.DELETE)
    public String deleteProfile(@RequestHeader("Authorization") String authHeader, 
    		@PathVariable(name = "language") String language,
    		@PathVariable(name = "id") String id) {
		
		return delete(language, id, false);
    } 
	
	private String delete(String language, String id, boolean isSimulation) {
		List<String> deletedRepo = new ArrayList<String>();
		List<String> updatedRepo = new ArrayList<String>();
		List<Repository> repos = repositoryDao.findAllByUserId(id);
		if(repos != null) {
			for(Repository repo : repos) {
				List<RepositoryUser> users = repo.getUsers();
				removeUser(users, id);
				if(isUserOnlyOneEditor(users)) {
					if(!isSimulation) {
						// delete repository and related reports
						repositoryDao.delete(id);
						certificationReportDao.deleteByRepositoryId(id);
					}
					deletedRepo.add(repo.getName());
				} else {
					if(!isSimulation) {
						// save repository user removed from the list
						repo.setUsers(users);
						repositoryDao.save(repo);
					}
					updatedRepo.add(repo.getName());
				}
			}
		}
		if(!isSimulation) {
			profileDao.delete(id);
			if(adminDao.isAdmin(id) || adminDao.isSuperAdmin(id)) {
				adminDao.delete(id);
			}
		}
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
		
		StringBuilder result = new StringBuilder();
		if(updatedRepo.size() > 0) {
			if(isSimulation) {
				result.append(messages.getString("user.information.update.repo.warning")).append(" ");	
			} else {
				result.append(messages.getString("user.information.update.repo")).append(" ");
			}
			for(int i=0 ; i<updatedRepo.size() ; i++) {
				result.append(updatedRepo.get(i));
				if(i < updatedRepo.size()-1) {
					result.append(", ");
				} else {
					result.append("<br/>");
				}
			}
		}
		if(deletedRepo.size() > 0) {
			if(isSimulation) {
				result.append(messages.getString("user.information.repo.deleted.warning")).append(" ");
			} else {
				result.append(messages.getString("user.information.repo.deleted")).append(" ");
			}
			for(int i=0 ; i<deletedRepo.size() ; i++) {
				result.append(deletedRepo.get(i));
				if(i < deletedRepo.size()-1) {
					result.append(", ");
				} else {
					result.append("<br/>");
				}
			}
		}
		return result.toString();
	}
	
	/**
	 * @param users updated user list. The current user has been removed.
	 * @return true if the user was the only one editor
	 */
	private boolean isUserOnlyOneEditor(List<RepositoryUser> users) {
		boolean result = true;
		if(users != null) {
			int count = 0;
			for(RepositoryUser user : users) {
			    if (StringUtils.equals(Roles.EDITOR, user.getRole())) {
			    	count++;
			    	if(count >= 1) {
			    		result = false;
			    		break;
			    	}
			    }
			}
		}
		return result;
	}

	/**
	 * Remove user from the repository user list
	 * @param users list for a repository
	 * @param id userId
	 */
	private void removeUser(List<RepositoryUser> users, String id) {;
		if(users != null) {
			for (Iterator<RepositoryUser> iter = users.listIterator(); iter.hasNext(); ) {
				RepositoryUser ru = iter.next();
			    if (StringUtils.equals(id, ru.getId())) {
			        iter.remove();
			    }
			}
		}
	}
}
