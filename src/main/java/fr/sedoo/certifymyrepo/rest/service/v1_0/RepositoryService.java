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

import org.apache.commons.lang3.StringUtils;
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
import fr.sedoo.certifymyrepo.rest.dao.AffiliationDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.AccessRequest;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
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
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;
import fr.sedoo.certifymyrepo.rest.service.notification.EmailSender;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.BadRequestException;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.ForbiddenException;

@RestController
@CrossOrigin
@RequestMapping(value = "/repository/v1_0")
public class RepositoryService {

	private static final Logger LOG = LoggerFactory.getLogger(RepositoryService.class);

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
	
	//FIXME use NotificationService instead
	@Autowired
	private MailConfig mailConfig;
	

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
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getRepository/{id}", method = RequestMethod.GET)
	public RepositoryDto getRepository(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "id") String id) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		Repository repo = null;
		if (loggedUser.isAdmin()) {
			repo = repositoryDao.findById(id);
		} else {
			repo = repositoryDao.findByIdAndUserId(id, loggedUser.getUserId());
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
					repoUserDto.add(new RepositoryUserDto(user.getId(), userProfile.get().getName(), user.getRole()));
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
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/listAllFullRepositories", method = RequestMethod.GET)
	public List<FullRepositoryDto> listAllFullRepositories(@RequestHeader("Authorization") String authHeader) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		
		List<Repository> repos = null;
		if (loggedUser.isAdmin()) {
			repos = repositoryDao.findAll();
		} else {
			repos = repositoryDao.findAllByUserId(loggedUser.getUserId());
		}
		return getHealthInformationList(repos, loggedUser.getUserId(), loggedUser.isAdmin());
	}
	
	/**
	 * Return a list of repository elements linked to the logged user
	 * @param authHeader
	 * @return {@link Repository}
	 */
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/listMyRepositories", method = RequestMethod.GET)
	public List<RepositoryDto> listMyRepositories(@RequestHeader("Authorization") String authHeader) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		List<Repository> repos =  repositoryDao.findAllByUserId(loggedUser.getUserId());
		List<RepositoryDto> result = new ArrayList<>();
		for(Repository repo : repos) {
			Affiliation affiliation = null;
			if(repo.getAffiliationId() != null)
				affiliation = affiliationDao.findById(repo.getAffiliationId());
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
				full.setRepository(new RepositoryDto(repo, new AffiliationDto(affiliation)));
				CertificationReport lastRepoNotValidated = certificationReportDao.findReportInProgressByRepositoryIdAndMaxUpdateDate(repo.getId());
				full.setHealthLatestInProgressReport(repositoryHealthCheck(lastRepoNotValidated));
				
				CertificationReport lastRepoValidated = certificationReportDao.findReportValidatedByRepositoryIdAndMaxUpdateDate(repo.getId());
				full.setHealthLatestValidReport(repositoryHealthCheck(lastRepoValidated));
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
			
			ReportDto latestReport = new ReportDto(report);
			result.setLatestReport(latestReport);
			
			CertificationTemplate template = templateDao.getCertificationReportTemplate(report.getTemplateId());
			latestReport.setLevelMaxValue(template.getLevels().size() - 1);
			List<CertificationItemDto> itemList = new ArrayList<CertificationItemDto>();
			for(CertificationItem item : report.getItems()) {
				CertificationItemDto itemDto = new CertificationItemDto(item);
				for(RequirementTemplate requirementTemplate : template.getRequirements()) {
					if(StringUtils.equals(item.getCode(), requirementTemplate.getCode())) {
						itemDto.setLevelActive(requirementTemplate.isLevelActive());
					}
				}
				itemList.add(itemDto);
				String level = (item.getLevel() != null && item.getLevel() != null)
						? item.getLevel() : null;
				incrementCounter(avg, level);
			}
			latestReport.setItems(itemList);
			
			// Green: all the requirements are at the level 4, 3 or 0 for not applicable.
			// Orange: at list half of the requirements has been considered (level > 1 or 0 for not applicable).
			// Red: more than half of the requirements has not been considered yet.
			if((!avg.containsKey("null") || avg.get("null") == 0) 
					&& (!avg.containsKey("1") || avg.get("1") == 0 )
					&& (!avg.containsKey("2") || avg.get("2") == 0)) {
				result.setGreen(Boolean.TRUE);
			} else {
				int occurencies = 0;
				if(avg.containsKey("null")) {
					occurencies = avg.get("null");
				}
				if(avg.containsKey("1")) {
					occurencies += avg.get("1");
				}
				if(occurencies <= report.getItems().size() / 2) {
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
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Repository save(@RequestHeader("Authorization") String authHeader,
			@RequestBody Repository repository,
			@RequestParam String language) {
		Repository result = null;
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
		if(repository != null) {
			Repository repo = repositoryDao.findByName(repository.getName());
			if(repo == null || StringUtils.equals(repository.getId(), repo.getId())) {
				if(repository.getId() != null) {
					checkUsersNotification(repository, messages);
				}
				if(repository.getId() == null) {
					repository.setCreationDate(new Date());
				}
				result =  repositoryDao.save(repository);	
			} else {
				throw new BusinessException(String.format(messages.getString("repository.duplicate.error"), repo.getName()));
			}
		} else {
			LOG.error("Repository parameter cannot be null");
			throw new BadRequestException();
		}
		return result;
	}
	
	/**
	 * Before saving the repository check if any users have been added or removed
	 * Then a notification if needed
	 * @param repository updated
	 * @param messages i18n
	 */
	private void checkUsersNotification(Repository repository, ResourceBundle messages) {
		Repository existingRepo = null;
		if(repository.getId() != null) {
			existingRepo = repositoryDao.findById(repository.getId());
		}
		if(existingRepo != null) {
			// List user id already in DB
			Set<String> alreadyInDBUserId = existingRepo.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
	
			// List user id potentially updated
			Set<String> newUserId = repository.getUsers().stream().map(repoUser -> repoUser.getId()).collect(Collectors.toSet());
	
			// Identify symmetric differences
			Set<String> symmetricDiff = new HashSet<String>(alreadyInDBUserId);
			symmetricDiff.addAll(newUserId);
			Set<String> tmp = new HashSet<String>(alreadyInDBUserId);
			tmp.retainAll(newUserId);
			symmetricDiff.removeAll(tmp);
	
			// Identify deleted id
			Set<String> deletedId = new HashSet<String>(alreadyInDBUserId);
			deletedId.retainAll(symmetricDiff);
			for (String userId : deletedId) {
				Optional<Profile> userProfile = profileDao.findById(userId);
				if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
					ContactDto contact = new  ContactDto();
					Set<String> to = new HashSet<String>();
					to.add(userProfile.get().getEmail());
					contact.setTo(to);
					contact.setSubject(String.format(messages.getString("remove.user.notification.subject"), repository.getName()));
					contact.setMessage(String.format(messages.getString("remove.user.notification.content"), repository.getName()));
					emailSender.sendNotification(contact);
				}
			}
			
			// Identify added id
			Set<String> addedId = new HashSet<String>(newUserId);
			addedId.retainAll(symmetricDiff);
			for (String userId : addedId) {
				Optional<Profile> userProfile = profileDao.findById(userId);
				if(userProfile.isPresent() && userProfile.get().getEmail() != null) {
					ContactDto contact = new  ContactDto();
					Set<String> to = new HashSet<String>();
					to.add(userProfile.get().getEmail());
					contact.setTo(to);
					contact.setSubject(String.format(messages.getString("add.user.notification.subject"), repository.getName()));
					contact.setMessage(String.format(messages.getString("add.user.notification.content"), repository.getName(), getUserRole(repository.getUsers(), userId)));
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
					contact.setSubject(String.format(messages.getString("add.user.notification.subject"), repository.getName()));
					contact.setMessage(String.format(messages.getString("add.user.notification.content"), repository.getName(), getUserRole(repository.getUsers(), userId)));
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
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public void delete(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "id") String  id) {
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
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public List<Repository> search(@RequestHeader("Authorization") String authHeader, @RequestParam List<String> keywords) {
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
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/requestAccess", method = RequestMethod.POST)
	public void requestAccess(@RequestHeader("Authorization") String authHeader, 
			@RequestBody AccessRequest accessRequest,
			@RequestParam String language) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
		
		Repository repo = repositoryDao.findById(accessRequest.getRepositoryId());
		if (repo != null) {
			
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
			contact.setSubject(String.format(messages.getString("repository.access.request.subject"), 
					messages.getString(accessRequest.getRole()), repo.getName(), accessRequest.getUserName()));
			contact.setMessage(String.format(messages.getString("repository.access.request.content"), 
					accessRequest.getUserName(), accessRequest.getEmail(),
					messages.getString(accessRequest.getRole()), 
					repo.getName(), accessRequest.getText()));
			emailSender.sendNotification(contact);
		} else {
			LOG.error("No repository found. No access can be granted.");
		}

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
