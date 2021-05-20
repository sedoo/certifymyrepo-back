package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import fr.sedoo.certifymyrepo.rest.dao.CommentsDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Comment;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
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
	private AdminDao adminDao;
	
	@Autowired
	private CommentsDao commentsDao;
	
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
		Optional<Profile> profile = profileDao.findById(LoginUtils.getLoggedUser().getUserId());
		if(profile.isPresent()) {
			return profile.get();
		} else {
			return null;
		}
    }
	
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/saveProfile", method = RequestMethod.POST)
    public Profile saveProfile(@RequestHeader("Authorization") String authHeader, 
    		@RequestBody Profile profile,
    		@RequestParam String language) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        
		if(profile.getEmail() != null) {
			Profile existingProfile = profileDao.findByEmail(profile.getEmail());
			if(existingProfile != null && !StringUtils.equals(profile.getId(), existingProfile.getId())) {
				throw new BusinessException(messages.getString("create.user.error.duplicate.email").concat(profile.getName()));
			}
		}
		if(profile.getOrcid() != null) {
			Profile existingProfile = profileDao.findByOrcid(profile.getOrcid());
			if(existingProfile != null && !StringUtils.equals(profile.getId(), existingProfile.getId())) {
				throw new BusinessException(messages.getString("create.user.error.duplicate.orcid").concat(profile.getName()));
			}
		}
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
			throw new BusinessException(messages.getString("create.user.error.duplicate.email").concat(profile.getName()));
		}
		if(profile.getOrcid() != null && profileDao.findByOrcid(profile.getOrcid()) != null) {
			throw new BusinessException(messages.getString("create.user.error.duplicate.orcid").concat(profile.getName()));
		}
		Profile createdProfile = profileDao.save(profile);
		
		if(createdProfile.getEmail() != null) {
			List<String> superAdminEmails = new ArrayList<String>();
			List<Admin> superAdmins = adminDao.findAllSuperAdmin();
			for(Admin superAdmin : superAdmins) {
				Optional<Profile> userProfile = profileDao.findById(superAdmin.getUserId());
				if(userProfile.isPresent()) {
					superAdminEmails.add(userProfile.get().getEmail());
				}
				
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
	public List<Profile> listAll(@RequestHeader("Authorization") String authHeader) {
		//List<User> result = new ArrayList<>();
		List<Profile> usersProfile = profileDao.findAll();
		/**
		if(usersProfile != null) {
			for( Profile userProfile : usersProfile) {
				User user = (User) userProfile;
				user.setUserId(userProfile.getId());
				user.setEmail(userProfile.getEmail());
				user.setName(userProfile.getName());
				result.add(user);
			}
		}*/
		return usersProfile;
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
						removeUserNameFromComments(userId);
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
			result.append(createInformationContent("update", messages, deletedRepo));
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
	
	/**
	 * Set user name to null for all the comments of the given userId
	 * @param userId id of the user collection
	 */
	private void removeUserNameFromComments(String userId) {
		List<RequirementComments> allRequirementscomments = commentsDao.getCommentsByUserId(userId);
		if(allRequirementscomments != null && allRequirementscomments.size() > 0) {
			for(RequirementComments singleRequirementComments : allRequirementscomments) {
				for(Comment comment : singleRequirementComments.getComments()) {
					if(StringUtils.equals(comment.getUserId(), userId)) {
						comment.setUserName(null);
					}
				}
			}
		}
	}
}
