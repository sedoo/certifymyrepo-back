package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.config.SsoPermissionEvaluator;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.AffiliationDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.AccessRequest;
import fr.sedoo.certifymyrepo.rest.domain.Affiliation;
import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.dto.AffiliationDto;
import fr.sedoo.certifymyrepo.rest.dto.CertificationItemDto;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;
import fr.sedoo.certifymyrepo.rest.dto.FullRepositoryDto;
import fr.sedoo.certifymyrepo.rest.dto.ReportDto;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryDto;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryHealth;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryUserDto;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.notification.EmailSender;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.BadRequestException;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping(value = "/repository/v1_0")
public class RepositoryService {

	@Autowired
	SsoPermissionEvaluator permissionEvaluator;
	
	@Autowired
	RepositoryDao repositoryDao;
	
	@Autowired
	CertificationReportDao certificationReportDao;
	
	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	AdminDao adminDao;
	
	@Autowired
	CertificationReportTemplateDao templateDao;
	
	@Autowired
	private AffiliationDao affiliationDao;
	
	@Autowired
	private ApplicationConfig appConfig;
	
	@Value("${app.url}")
	private String appUrl;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}
	
	/**
	 * Return a repository element
	 * @param authHeader
	 * @param id repository identifier
	 * @return {@link Repository}
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/getRepository/{id}", method = RequestMethod.GET)
	public RepositoryDto getRepository(HttpServletRequest request, @PathVariable(name = "id") String id) {
		Profile loggedUser = permissionEvaluator.getUser(request);
		Repository repo = null;
		if (permissionEvaluator.isAdmin(request)) {
			repo = repositoryDao.findById(id);
		} else {
			repo = repositoryDao.findByIdAndUserId(id, loggedUser.getId());
		}
		Affiliation affiliation = null;
		if(repo != null) {
			if(repo.getAffiliationId() != null)
				affiliation = affiliationDao.findById(repo.getAffiliationId());
			RepositoryDto repositoryDto = new RepositoryDto(repo, new AffiliationDto(affiliation));
			List<RepositoryUserDto> repoUserDto = new ArrayList<RepositoryUserDto>();
			for(RepositoryUser user : repo.getUsers()) {
				Optional<Profile> userProfile = profileDao.findById(user.getId());
				if(userProfile.isPresent()) {
					repoUserDto.add(new RepositoryUserDto(user.getId(),
							userProfile.get().getName(), userProfile.get().getEmail(),
							user.getRole(), user.getStatus()));
				}
			}
			repositoryDto.setUsers(repoUserDto);
			return repositoryDto;
		} else {
			return null;
		}
		
	}
	
	/**
	 * Return a list of repository elements with additional information: 
	 * <li>health check from the latest report</li>
	 * <li>boolean isReadOnly if the logged user has the role READER on the repository</li>
	 * @param authHeader
	 * @return {@link FullRepositoryDto}
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/listAllFullRepositories", method = RequestMethod.GET)
	public List<FullRepositoryDto> listAllFullRepositories(HttpServletRequest request) {
		
		Profile user = permissionEvaluator.getUser(request);
		List<Repository> repos = null;
		boolean isAdmin = permissionEvaluator.isAdmin(request);
		if (isAdmin) {
			repos = repositoryDao.findAll();
		} else {
			repos = repositoryDao.findAllByUserId(user.getId());
		}
		return getHealthInformationList(repos, user.getId(), isAdmin);
	}
	
	/**
	 * Return a list of repository elements linked to the logged user
	 * @param authHeader
	 * @return {@link Repository}
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/listMyRepositories", method = RequestMethod.GET)
	public List<RepositoryDto> listMyRepositories(HttpServletRequest request) {
		
		Profile user = permissionEvaluator.getUser(request);
		List<Repository> repos =  repositoryDao.findAllByUserId(user.getId());
		List<RepositoryDto> result = new ArrayList<>();
		for(Repository repo : repos) {
			Affiliation affiliation = null;
			if(repo.getAffiliationId() != null) {
				affiliation = affiliationDao.findById(repo.getAffiliationId());
			}
			result.add(new RepositoryDto(repo, new AffiliationDto(affiliation)));
		}
		
		return result;
	}
	
	/**
	 * create a list of FullRepository containing the Repository document as well as health informations
	 * @param repos
	 * @param loggedUserId 
	 * @return List<FullRepository>
	 */
	private List<FullRepositoryDto> getHealthInformationList(List<Repository> repos, String loggedUserId, boolean isSuperAdmin) {
		if(repos != null && repos.size() > 0) {
			List<FullRepositoryDto> result = new ArrayList<FullRepositoryDto>();
			for (Repository repo : repos) {
				FullRepositoryDto full = new FullRepositoryDto();
				Affiliation affiliation = null;
				if(repo.getAffiliationId() != null)
					affiliation = affiliationDao.findById(repo.getAffiliationId());
				full.setName(repo.getName());
				full.setCreationDate(repo.getCreationDate());
				full.setRepository(new RepositoryDto(repo, new AffiliationDto(affiliation)));
				CertificationReport lastRepoNotValidated = certificationReportDao.findReportInProgressByRepositoryIdAndMaxUpdateDate(repo.getId());
				full.setHealthLatestInProgressReport(repositoryHealthCheck(lastRepoNotValidated));
				if(lastRepoNotValidated != null) {
					full.setLatestInProgressReportUpdateDate(lastRepoNotValidated.getUpdateDate());
				}

				CertificationReport lastRepoValidated = certificationReportDao.findReportValidatedByRepositoryIdAndMaxUpdateDate(repo.getId());
				full.setHealthLatestValidReport(repositoryHealthCheck(lastRepoValidated));
				if(lastRepoValidated != null) {
					full.setLatestValidReportUpdateDate(lastRepoValidated.getUpdateDate());
				}
				
				// Repositories are read only by default
				// only SuperAdmin or user(Editor) associated with a repository have write access
				full.setReadonly(true);
				if(isSuperAdmin 
						|| repo.getUsers().stream().anyMatch(
								s -> StringUtils.equals(s.getId(),loggedUserId) 
									&& StringUtils.equals(s.getRole(),Roles.EDITOR)) ) {
					full.setReadonly(false);
				}
				result.add(full);
			}
			return result;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param report
	 * @param numberOfLevel 
	 * @return health check indicator, label and data list for radar chart
	 */
	private RepositoryHealth repositoryHealthCheck(CertificationReport report) {
		if(report != null ) {
			
			RepositoryHealth result = new RepositoryHealth();
			// init counters
			Map<String, Integer> avg = new HashMap<String, Integer>();
			
			CertificationTemplate template = templateDao.getCertificationReportTemplate(report.getTemplateId());
			ReportDto latestReport = new ReportDto(report, template);
			result.setLatestReport(latestReport);
			
			List<CertificationItemDto> itemList = new ArrayList<CertificationItemDto>();
			for(CertificationItem item : report.getItems()) {
				CertificationItemDto itemDto = new CertificationItemDto(item);
				for(RequirementTemplate requirementTemplate : template.getRequirements()) {
					if(StringUtils.equals(item.getCode(), requirementTemplate.getCode())) {
						itemDto.setLevelActive(requirementTemplate.isLevelActive());
					}
				}
				itemList.add(itemDto);
				if(itemDto.isLevelActive()) {
					String level = (item.getLevel() != null && item.getLevel() != null)
							? item.getLevel() : null;
					incrementCounter(avg, level);
				}

			}
			latestReport.setItems(itemList);
			
			// Green: all the requirements are at the level 4, 3 or 0 for not applicable.
			// Orange: at least half of the requirements has got a level 4, 3 or 0 for not applicable.
			// Red: more than half of the requirements has got a level lower than 3 or has not been filled yet.
			if((!avg.containsKey("null") || avg.get("null") == 0) 
					&& (!avg.containsKey("1") || avg.get("1") == 0 )
					&& (!avg.containsKey("2") || avg.get("2") == 0)) {
				result.setGreen(Boolean.TRUE);
			} else {
				int occurencies = 0;
				if(avg.containsKey("4")) {
					occurencies += avg.get("4");
				}
				if(avg.containsKey("3")) {
					occurencies += avg.get("3");
				}
				if(avg.containsKey("0")) {
					occurencies += avg.get("0");
				}
				if(occurencies >= report.getItems().size() / 2) {
					result.setOrange(Boolean.TRUE);
				} else {
					result.setRed(Boolean.TRUE);
				}
			}

			return result;
		} else {
			return null;
		}
	}

	/**
	 * Key: Compliance level
	 * Value : number of requirement at this compliance level
	 * @param map
	 * @param key level
	 */
	private void incrementCounter(Map<String, Integer> map, String key) {
		String localKey = key;
		if(key == null) {
			localKey = "null";
		}
		map.putIfAbsent(localKey, 0);
		map.put(localKey, map.get(localKey)+1);
	}
	
	/**
	 * Save a Repository element
	 * @param authHeader authorization token form logged user
	 * @param repository element to be saved
	 * @return the saved element
	 * @throws {@link BadRequestException} if the element to be saved is null
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Repository save(HttpServletRequest request,
			@RequestBody(required = true) Repository repository,
			@RequestParam String language) {
		Repository result = null;
		Repository repo = repositoryDao.findByName(repository.getName());
		if(repo == null || StringUtils.equals(repository.getId(), repo.getId())) {
			if(repository.getId() != null) {
				checkUsersNotification(repository);
			}
			if(repository.getId() == null) {
				repository.setCreationDate(new Date());
			}
			result =  repositoryDao.save(repository);	
		} else {
	        ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale(language));
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, 
					String.format(messages.getString("repository.duplicate.error"), repo.getName()));
		}
		return result;
	}
	
	/**
	 * Before saving the repository check if any notification has to be sent
	 * <li>An user has been added on the repository</li>
	 * <li>An user has been removed from the repository</li>
	 * <li>An user access request has been declined</li>
	 * Then a notification if needed
	 * @param repository updated
	 */
	private void checkUsersNotification(Repository repository) {
        ResourceBundle messagesFr = ResourceBundle.getBundle("messages", new Locale("fr"));
        ResourceBundle messagesEn = ResourceBundle.getBundle("messages", new Locale("en"));
        
		Repository existingRepo = null;
		if(repository.getId() != null) {
			existingRepo = repositoryDao.findById(repository.getId());
		}
		if(existingRepo != null) {
	        
			// List user id already in DB
			Set<String> alreadyInDBUserIds = existingRepo.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
	
			// List user id potentially updated
			Set<String> newUserIds = repository.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
	
			// Identify symmetric differences
			Set<String> symmetricDiff = new HashSet<String>(alreadyInDBUserIds);
			symmetricDiff.addAll(newUserIds);
			Set<String> tmp = new HashSet<String>(alreadyInDBUserIds);
			tmp.retainAll(newUserIds);
			symmetricDiff.removeAll(tmp);
	
			// Identify deleted id
			Set<String> deletedIdList = new HashSet<String>(alreadyInDBUserIds);
			deletedIdList.retainAll(symmetricDiff);
			for (String userId : deletedIdList) {
				Optional<Profile> userProfile = profileDao.findById(userId);
				if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
					
					// Check the usre's status on this repository
					// it may be an actual user who has been remove
					// or a pending request which has been declined
					RepositoryUser existingRepoUser = existingRepo.getUsers().stream()
							  .filter(user -> userId.equals(user.getId()))
							  .findAny()
							  .orElse(null);
					
					ContactDto contact = new  ContactDto();
					Set<String> to = new HashSet<String>();
					to.add(userProfile.get().getEmail());
					contact.setTo(to);
					if(existingRepoUser != null && StringUtils.equals(RepositoryUser.PENDING, existingRepoUser.getStatus())) {
						contact.setSubject(String.format(appConfig.getDeclinedUserAccessNotificationSubject(), repository.getName(), repository.getName()));
						String content = appConfig.getEnglishHeader().concat("<br/><br/>");
						content = content.concat(String.format(appConfig.getDeclinedUserAccessNotificationFrenchContent(), repository.getName()))
									.concat("<br/><br/>").concat(String.format(appConfig.getDeclinedUserAccessNotificationEnglishContent(), repository.getName()));
						contact.setMessage(content);
					} else {
						contact.setSubject(String.format(appConfig.getRemoveUserNotificationSubject(), repository.getName(), repository.getName()));
						String content = appConfig.getEnglishHeader().concat("<br/><br/>");
						content = content.concat(String.format(appConfig.getRemoveUserNotificationFrenchContent(), repository.getName()))
									.concat("<br/><br/>").concat(String.format(appConfig.getRemoveUserNotificationEnglishContent(), repository.getName()));
						contact.setMessage(content);
					}
					emailSender.sendNotification(contact);
				}
			}
			
			// Identify added id
			Set<String> addedId = new HashSet<String>(newUserIds);
			addedId.retainAll(symmetricDiff);
			for (String userId : addedId) {
				Optional<Profile> userProfile = profileDao.findById(userId);
				if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
					ContactDto contact = new  ContactDto();
					Set<String> to = new HashSet<String>();
					to.add(userProfile.get().getEmail());
					contact.setTo(to);
					contact.setSubject(String.format(appConfig.getAddUserNotificationSubject(), repository.getName(), repository.getName()));
					String content = appConfig.getEnglishHeader().concat("<br\\><br\\>");
					content = content.concat(String.format(appConfig.getAddUserNotificationFrenchContent(), repository.getName(), messagesFr.getString(getUserRole(repository.getUsers(), userId))))
								.concat("<br\\><br\\>").concat(String.format(appConfig.getAddUserNotificationEnglishContent(), repository.getName(), messagesEn.getString(getUserRole(repository.getUsers(), userId))));
					contact.setMessage(content);
					emailSender.sendNotification(contact);
				}
			}
		} else {
			ApplicationUser loggedUser = LoginUtils.getLoggedUser();
			Set<String> newUserId = repository.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
			newUserId.remove(loggedUser.getUserId());
			for (String userId : newUserId) {
				Optional<Profile> userProfile = profileDao.findById(userId);
				if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
					ContactDto contact = new  ContactDto();
					Set<String> to = new HashSet<String>();
					to.add(userProfile.get().getEmail());
					contact.setTo(to);
					contact.setSubject(String.format(appConfig.getAddUserNotificationSubject(), repository.getName(), repository.getName()));
					String content = appConfig.getEnglishHeader().concat("<br\\><br\\>");
					content = content.concat(String.format(appConfig.getAddUserNotificationFrenchContent(), repository.getName(), messagesFr.getString(getUserRole(repository.getUsers(), userId))))
								.concat("<br\\><br\\>").concat(String.format(appConfig.getAddUserNotificationEnglishContent(), repository.getName(), messagesEn.getString(getUserRole(repository.getUsers(), userId))));
					contact.setMessage(content);
					emailSender.sendNotification(contact);
				}
			}
		}
	}
	
	/**
	 * @param users
	 * @param userId
	 * @return Role
	 */
	private String getUserRole(List<RepositoryUser> users, String userId) {
		String role = null;
		for(RepositoryUser user : users) {
			if(StringUtils.equals(user.getId(), userId)) {
				role = user.getRole();
			}
		}
		return role;
	}

	/**
	 * Delete a Repository element for a given id
	 * @param authHeader authorization token form logged user
	 * @param id of the repository
	 * @throws {@link ForbiddenException} if the user is not MANAGER or ADMIN
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public void delete(HttpServletRequest request, @PathVariable(name = "id") String  id) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		if (loggedUser.isAdmin()) {
			repositoryDao.delete(id);
			certificationReportDao.deleteByRepositoryId(id);
		} else {
			if (null != repositoryDao.findByIdAndUserId(id, loggedUser.getUserId())) {
				repositoryDao.delete(id);
				certificationReportDao.deleteByRepositoryId(id);
			} else {
				throw new ForbiddenException();
			}
		}
	}
	
	/**
	 * Search repository from a list of keywords.
	 * No check on the user role as an user use this search method to request access on a repository out of his scope
	 * @param authHeader authorization token form logged user
	 * @param keywords used by the search query
	 * @return a list of repository elements
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public List<Repository> search(HttpServletRequest request, @RequestParam List<String> keywords) {
		StringBuilder regex = new StringBuilder();
		if(keywords.size()>1) {
			for (int i=0; i<keywords.size()-1;i++ ) {
				regex.append(keywords.get(i)).append("|");
			}
			regex.append(keywords.get(keywords.size()-1));
		} else {
			regex = regex.append(keywords.get(0));
		}

		List<Repository> result = repositoryDao.findByNameOrKeywords(regex.toString());
		return result;
	}
	
	/**
	 * Search repository from a list of keywords.
	 * No check on the user role as an user use this search method to request access on a repository out of his scope
	 * @param authHeader authorization token form logged user
	 * @param accessRequest with repositoryId, orcid the user identifier, text optional  user's message, email user email and requested role
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/requestAccess", method = RequestMethod.POST)
	public void requestAccess(HttpServletRequest request,
			@RequestBody AccessRequest accessRequest,
			@RequestParam String language) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
		
		Repository repo = repositoryDao.findById(accessRequest.getRepositoryId());
		if (repo != null) {
			
			// Search for the user and check his status
			RepositoryUser repoUser = repo.getUsers().stream()
					  .filter(user -> accessRequest.getUserId().equals(user.getId()))
					  .findAny()
					  .orElse(null);
			
			if(repoUser != null) {
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "User already present in the repository");
			} else {
				// Add the user on the repository with the pending status
				repo.getUsers().add(new RepositoryUser(accessRequest.getUserId(), accessRequest.getRole(), RepositoryUser.PENDING));
				repositoryDao.save(repo);
				
				String link = appUrl.concat("/?token=").concat(generateToken())
						.concat("#/requestValidation/").concat(accessRequest.getRepositoryId())
						.concat("/").concat(accessRequest.getUserId());
				
				// Notify the repo editors
				ContactDto contact = new ContactDto();
				Set<String> to = new HashSet<String>();
				to.add(repo.getContact());
				List<String> repoManagerOrcid = getEditorUserId(repo.getUsers());
				if (repoManagerOrcid != null && repoManagerOrcid.size() > 0) {
					List<Profile> profiles = profileDao.findByOrcidIn(repoManagerOrcid);
					if(profiles != null && profiles.size() > 0) {
						to.addAll(profiles.stream().map(profile -> profile.getEmail()).collect(Collectors.toSet()));
					}
				}
				contact.setTo(to);
				contact.setSubject(String.format(appConfig.getRepositoryAccessSubject(), repo.getName(), repo.getName()));
				
				String content = appConfig.getEnglishHeader().concat("<br/><br/>");
				
				content = content.concat(String.format(appConfig.getRepositoryAccessFrenchContent(), 
						accessRequest.getUserName(), accessRequest.getEmail(),
						messages.getString(accessRequest.getRole()), 
						repo.getName(), getButton(link, "Accepter"), accessRequest.getText()));
				content = content.concat("<br/><br/>");
				content = content.concat(String.format(appConfig.getRepositoryAccessEnglishContent(), 
						accessRequest.getUserName(), accessRequest.getEmail(),
						messages.getString(accessRequest.getRole()), 
						repo.getName(), getButton(link, "Accept"), accessRequest.getText()));
				
				contact.setMessage(content);
				emailSender.sendNotification(contact);
			}
		} else {
			log.error("No repository found. No access can be granted.");
		}

	}
	
	/**
	 * Generate a token
	 * @return
	 * @throws Exception
	 */
	private String generateToken() {
		try {
			String token = null;
			return token;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "YCould not generate a token");
		}
	}
	
	private String getButton(String link, String label) {
        return "<table cellspacing=\"0\" cellpadding=\"0\"<tr> <td align=\"center\" height=\"40\" bgcolor=\"#5CB85C\" style=\"-webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; color: #ffffff; display: block;\">"
                + "    <a href=\""
                + link
                + "\" style=\"font-size:12px; font-weight: bold; font-family: Helvetica, Arial, sans-serif; text-decoration: none; line-height:40px; width:100%; display:inline-block\">"
                + "<span style=\"color: #FFFFFF;margin-right: 15px;margin-left: 15px;\">"
                + label
                + "</span></a></td></tr> </table>";
    }
	
	/**
	 * 
	 * @param authHeader
	 * @param repositoryId
	 * @param userId
	 * @return true if the user has been accepted, false if the user had not a pending request anymore
	 */
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/validRequest/{repositoryId}/{userId}", method = RequestMethod.GET)
	public boolean validRequest(HttpServletRequest request, 
			@PathVariable(name = "repositoryId") String repositoryId,
			@PathVariable(name = "userId") String userId) {
		boolean result = false;
		Repository repository = repositoryDao.findById(repositoryId);
		if(repository != null) {
			// Search for the user and check his status
			RepositoryUser repoUser = repository.getUsers().stream()
					  .filter(user -> userId.equals(user.getId()))
					  .findAny()
					  .orElse(null);
			if(repoUser != null) {
				if(StringUtils.equals(RepositoryUser.PENDING, repoUser.getStatus())) {
					// The user has been found and his status is pending.
					// the request can be accepted
					for(RepositoryUser user : repository.getUsers()) {
						if(StringUtils.equals(userId, user.getId())){
							user.setStatus(RepositoryUser.ACTIVE);
							repositoryDao.save(repository);
							result = true;
							break;
						}
					}
				}	
			} else {
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "User not found");
			}
		}
		return result;
	}

	/**
	 * 
	 * @param users of of users associate on a repository
	 * @return List of repository manager orcid
	 */
	private List<String> getEditorUserId(List<RepositoryUser> users) {
		List<String> userIdList = new ArrayList<String>();
		if(users!= null && users.size() > 0) {
			for(RepositoryUser user : users) {
				if(StringUtils.equals(user.getRole(), Roles.EDITOR)
					&& !userIdList.contains(user.getId())) {
					userIdList.add(user.getId());
				}
			}
		}
		return userIdList;
	}

}
