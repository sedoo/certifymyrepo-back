package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.AttachmentDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CommentsDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.notification.EmailSender;

@RestController
@CrossOrigin
@RequestMapping(value = "/profile/v1_0")
public class ProfileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProfileService.class);
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private AdminDao adminDao;
	
	@Autowired
	private CommentsDao commentsDao;
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	@Autowired
	private CertificationReportDao certificationReportDao;
	
	@Autowired
	private AttachmentDao ftpClient;
	
	@Autowired
	private EmailSender emailSender;
	
	@Autowired
	private ApplicationConfig appConfig;
	
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public Profile profile(HttpServletRequest request) {	
		Optional<Profile> profile = profileDao.findById(LoginUtils.getLoggedUser().getUserId());
		if(profile.isPresent()) {
			return profile.get();
		} else {
			return null;
		}
    }
	
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
    @RequestMapping(value = "/saveProfile", method = RequestMethod.POST)
    public Profile saveProfile(HttpServletRequest request, 
    		@RequestBody Profile profile,
    		@RequestParam String language) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        
		if(profile.getEmail() != null) {
			Profile existingProfile = profileDao.findByEmail(profile.getEmail());
			if(existingProfile != null && !StringUtils.equals(profile.getId(), existingProfile.getId())) {
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, 
						messages.getString("create.user.error.duplicate.email").concat(" ").concat(existingProfile.getName()));
			}
		}
		if(profile.getOrcid() != null) {
			Profile existingProfile = profileDao.findByOrcid(profile.getOrcid());
			if(existingProfile != null && !StringUtils.equals(profile.getId(), existingProfile.getId())) {
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, 
						messages.getString("create.user.error.duplicate.orcid").concat(" ").concat(existingProfile.getName()));
			}
		}
		return profileDao.save(profile);
    }
	
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
    @RequestMapping(value = "/createNewProfile", method = RequestMethod.POST)
    public Profile createNewProfile(HttpServletRequest request,
    		@RequestBody Profile profile,
    		@RequestParam String language) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        
		if(profile.getEmail() != null) {
			Profile foundProfile = profileDao.findByEmail(profile.getEmail());
			if(foundProfile != null) {
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, 
						messages.getString("create.user.error.duplicate.email").concat(" ").concat(foundProfile.getName()));
			}
		}
		if(profile.getOrcid() != null && profileDao.findByOrcid(profile.getOrcid()) != null) {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, 
					messages.getString("create.user.error.duplicate.orcid").concat(" ").concat(profile.getName()));
		}
		Profile createdProfile = profileDao.save(profile);
		
		if(createdProfile.getEmail() != null) {
			ContactDto contact = new ContactDto();
			Set<String> to = new HashSet<String>();
			to.add(createdProfile.getEmail());
			contact.setTo(to);
			contact.setSubject(appConfig.getCreateUserNotificationSubject());
			if(createdProfile.getOrcid() != null) {
				String content = appConfig.getEnglishHeader().concat("<br/><br/>");
				content = content.concat(String.format(appConfig.getCreateUserNotificationOrcidFrenchContent(), profile.getOrcid()))
							.concat("<br/><br/>").concat(String.format(appConfig.getCreateUserNotificationOrcidEnglishContent(), profile.getOrcid()));
				contact.setMessage(content);
			} else {
				String content = appConfig.getEnglishHeader().concat("<br/><br/>");
				content = content.concat(String.format(appConfig.getCreateUserNotificationRenaterFrenchContent(), profile.getEmail()))
							.concat("<br/><br/>").concat(String.format(appConfig.getCreateUserNotificationRenaterEnglishContent(), profile.getEmail()));
				contact.setMessage(content);
			}
			emailSender.sendNotification(contact);
		}
		return createdProfile;
    }
	
	//@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/listAllUsers", method = RequestMethod.GET)
	public List<Profile> listAll(HttpServletRequest request) {
		List<Profile> usersProfile = profileDao.findAll();
		return usersProfile;
	}
	
	/**
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
    @RequestMapping(value = "/deleteProfileSimulation/{language}/{id}", method = RequestMethod.GET)
    public String deleteProfileSimulation(HttpServletRequest request, 
    		@PathVariable(name = "language") String language,
    		@PathVariable(name = "id") String id) {
		
		return delete(language, id, true);
	}
	
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
    @RequestMapping(value = "/deleteProfile/{language}/{id}", method = RequestMethod.DELETE)
    public String deleteProfile(HttpServletRequest request,
    		@PathVariable(name = "language") String language,
    		@PathVariable(name = "id") String id) {
		
		return delete(language, id, false);
    } 
	
	/**
	 * 
	 * @param language of the user making this action
	 * @param userId user identifier to delete
	 * @param isSimulation if true data will not be updated or deleted but a message will be return
	 * @return message to display in the UI
	 */
	private String delete(String language, String userId, boolean isSimulation) {
		List<String> deletedRepo = new ArrayList<String>();
		List<String> updatedRepo = new ArrayList<String>();
		List<Repository> repos = repositoryDao.findAllByUserId(userId);
		if(repos != null) {
			for(Repository repo : repos) {
				List<RepositoryUser> users = repo.getUsers();
				removeUser(users, userId);
				if(isUserOnlyOneEditor(users)) {
					if(!isSimulation) {
						// delete repository and related reports
						repositoryDao.delete(repo.getId());
						List<CertificationReport> reports = certificationReportDao.deleteByRepositoryId(repo.getId());
						ftpClient.deleteAllFilesInFolder(repo.getId());
						if(reports != null) {
							for(CertificationReport report : reports) {
								commentsDao.deleteByReportId(report.getId());
							}
						}
					}
					deletedRepo.add(repo.getName());
				} else {
					if(!isSimulation) {
						// save repository user removed from the list
						repo.setUsers(users);
						repositoryDao.save(repo);
						//removeUserNameFromComments(userId);
					}
					updatedRepo.add(repo.getName());
				}
			}
		}
		if(!isSimulation) {
			profileDao.delete(userId);
			if(adminDao.isAdmin(userId) || adminDao.isSuperAdmin(userId)) {
				adminDao.deleteByUserId(userId);
			}
		}
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
		
		StringBuilder result = new StringBuilder();
		if(updatedRepo.size() > 0 && isSimulation) {
			result.append(createInformationContent("update", messages, updatedRepo));
		}
		if(deletedRepo.size() > 0 && isSimulation) {
			result.append(createInformationContent("delete", messages, deletedRepo));
		}
		return result.toString();
	}
	
	/**
	 * @param key 'update' or 'delete'
	 * @param messages localized messages
	 * @param repo list if repositories to update or delete
	 * @return message to display in the UI
	 */
	private StringBuilder createInformationContent(String key, ResourceBundle messages, List<String> repo) {
		StringBuilder message = new StringBuilder();
		if(repo.size() > 0) {
			message.append(messages.getString("user.information.repo.".concat(key).concat(".warning"))).append(" ");	
			for(int i=0 ; i<repo.size() ; i++) {
				message.append(repo.get(i));
				if(i < repo.size()-1) {
					message.append(", ");
				} else {
					message.append("<br/>");
				}
			}
		}
		return message;
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
