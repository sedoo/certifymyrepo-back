package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.dao.AffiliationDao;
import fr.sedoo.certifymyrepo.rest.dao.AttachmentDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.dao.CommentsDao;
import fr.sedoo.certifymyrepo.rest.dao.ConnectedUserDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.Affiliation;
import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Comment;
import fr.sedoo.certifymyrepo.rest.domain.ConnectedUser;
import fr.sedoo.certifymyrepo.rest.domain.MyReport;
import fr.sedoo.certifymyrepo.rest.domain.MyReports;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.LevelTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.TemplateName;
import fr.sedoo.certifymyrepo.rest.dto.AffiliationDto;
import fr.sedoo.certifymyrepo.rest.dto.CertificationItemDto;
import fr.sedoo.certifymyrepo.rest.dto.CommentDto;
import fr.sedoo.certifymyrepo.rest.dto.ReportDto;
import fr.sedoo.certifymyrepo.rest.dto.RequirementCommentsDto;
import fr.sedoo.certifymyrepo.rest.export.CommentExport;
import fr.sedoo.certifymyrepo.rest.export.PdfPrinter;
import fr.sedoo.certifymyrepo.rest.export.Report;
import fr.sedoo.certifymyrepo.rest.export.Requirement;
import fr.sedoo.certifymyrepo.rest.ftp.DomainFilter;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;
import fr.sedoo.certifymyrepo.rest.service.notification.EmailSender;
import fr.sedoo.certifymyrepo.rest.service.notification.NotificationUtils;
import fr.sedoo.certifymyrepo.rest.utils.MimeTypeUtils;

@RestController
@CrossOrigin
@RequestMapping(value = "/certificationReport/v1_0")
public class CertificationReportService {

	private static final Logger LOG = LoggerFactory.getLogger(CertificationReportService.class);

	@Autowired
	private CertificationReportDao certificationReportDao;
	
	@Autowired
	private RepositoryDao repositoryDao;
	
	@Autowired
	private CommentsDao commentsDao;
	
	@Autowired
	ProfileDao profileDao;
	
	@Autowired
	EmailSender emailSender;
	
	@Autowired
	private AttachmentDao ftpClient;
	
	@Autowired
	private CertificationReportTemplateDao certificationReportTemplateDao;
	
	@Autowired
	private PdfPrinter pdfPrinter;
	
	@Autowired
	private ApplicationConfig appConfig;
	
	@Autowired
	private AffiliationDao affiliationDao;
	
	@Autowired
	private NotificationUtils notificationUtils;
	
	@Autowired
	private ConnectedUserDao connectedUserDao;
	
	@Value("${temporary.folder}")
	String temporaryFolderName;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}
	
	/**
	 * 
	 * @param authHeader
	 * @param repositoryId
	 * @return list of all reports for a given repository and rights of the user on this repository
	 */
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/listByRepositoryId/{repositoryId}", method = RequestMethod.GET)
	public MyReports listByRepositoryId(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "repositoryId") String repositoryId) {
		MyReports result = new MyReports();
		
		// Get All the reports from the given repository id
		List<CertificationReport> reports = certificationReportDao.findByRepositoryId(repositoryId);
		
		// Transform report collection into DTO object. 
		// Add information from template (number of levels, level active for a given requirement etc)
		List<ReportDto> reportsDto = new ArrayList<ReportDto>();
		for(CertificationReport report : reports) {
			// For a given repository, reports can be made from different template
			CertificationTemplate template = certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateId());
			ReportDto reportDto = new ReportDto(report, template);
			List<CertificationItemDto> itemList = new ArrayList<CertificationItemDto>();
			for(CertificationItem item : report.getItems()) {
				CertificationItemDto itemDto = new CertificationItemDto(item);
				for(RequirementTemplate requirementTemplate : template.getRequirements()) {
					if(StringUtils.equals(item.getCode(), requirementTemplate.getCode())) {
						itemDto.setLevelActive(requirementTemplate.isLevelActive());
					}
				}
				itemList.add(itemDto);
			}
			reportDto.setItems(itemList);
			reportsDto.add(reportDto);
		}
		result.setReports(reportsDto);
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		// super admin has all rights
		if (loggedUser.isSuperAdmin()) {
			result.setEditExistingAllowed(true);
			result.setCreationValidationAllowed(true);
		} else {
			// an admin (COSO co-pilot) has only read access if he is not declared as user on the repository
			result.setEditExistingAllowed(false);
			result.setCreationValidationAllowed(false);
			Repository repo = repositoryDao.findByIdAndUserId(repositoryId, loggedUser.getUserId());
			if (null != repo) {
				for( RepositoryUser user : repo.getUsers()) {
					if(StringUtils.equals(user.getId(),loggedUser.getUserId())) {
						if(StringUtils.equals(Roles.EDITOR, user.getRole())) {
							result.setEditExistingAllowed(true);
							result.setCreationValidationAllowed(true);
						} else if(StringUtils.equals(Roles.CONTRIBUTOR, user.getRole())) {
							result.setEditExistingAllowed(true);
							result.setCreationValidationAllowed(false);
						} else {
							result.setEditExistingAllowed(false);
							result.setCreationValidationAllowed(false);
						}
						break;
					}
				}
			} else if(!loggedUser.isAdmin()) {
				LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), repositoryId));
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to access thoses reports");
			}
		}
		return result;
	}	
	
	/**
	 * @param authHeader
	 * @return list of all templates
	 */
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getTemplatesList", method = RequestMethod.GET)
	public List<TemplateName> getTemplateList(@RequestHeader("Authorization") String authHeader) {
		return certificationReportTemplateDao.getTemplateNameList();
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getReport/{reportId}", method = RequestMethod.GET)
	public MyReport getReport(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "reportId") String id) {
		
		MyReport result = new MyReport();
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		
		CertificationReport report = certificationReportDao.findById(id);
		result.setReport(report);
		

		
		if(ReportStatus.RELEASED.equals(report.getStatus())) {
			result.setEditExistingAllowed(false);
			result.setValidationAllowed(false);
		} else {
			String currentEditor = isUserEditingReport(id, loggedUser.getUserId());
			if(null != currentEditor) { 
				result.setEditExistingAllowed(false);
				result.setValidationAllowed(false);
				result.setCurrentEditor(currentEditor);
			} else if (loggedUser.isSuperAdmin()) {
				// super admin has all rights unless if the status is release no body can edit or re-validate it
				result.setEditExistingAllowed(true);
				result.setValidationAllowed(true);
			} else {
				// an admin (COSO co-pilot) has only read access if he is not declared as user on the repository
				result.setEditExistingAllowed(false);
				result.setValidationAllowed(false);
				if(report != null) {
					Repository repo = repositoryDao.findByIdAndUserId(report.getRepositoryId(), loggedUser.getUserId());
					if (null != repo) {
						for( RepositoryUser user : repo.getUsers()) {
							if(StringUtils.equals(user.getId(),loggedUser.getUserId())) {
								if(StringUtils.equals(Roles.EDITOR, user.getRole())) {
									result.setEditExistingAllowed(true);
									result.setValidationAllowed(true);
								} else if(StringUtils.equals(Roles.CONTRIBUTOR, user.getRole())) {
									result.setEditExistingAllowed(true);
									result.setValidationAllowed(false);
								} else {
									result.setEditExistingAllowed(false);
									result.setValidationAllowed(false);
								}
								break;
							}
						}
					} else if(!loggedUser.isAdmin()) {
						LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), report.getRepositoryId()));
						throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to access this report");
					}
				}
			}
		}

		result.setRequirementComments(commentsDao.getCommentsByReportId(id));
		result.setTemplate(certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateId()));
		result.setAttachments(ftpClient.listFiles(id));
		return result;
	}
	
	/**
	 * 
	 * @param reportId
	 * @param userId
	 * @return true if the report is currently edited
	 */
	private String isUserEditingReport(String reportId, String userId) {
		String editor = null;
		List<ConnectedUser> res = connectedUserDao.getConnectedUsersByReportId(reportId);
		if(res != null && !res.isEmpty()) {
			List<ConnectedUser> editingUser = res.stream().filter(u -> !StringUtils.equals(u.getUserId(), userId)).collect(Collectors.toList());
			if(editingUser != null && editingUser.size() > 0) {
				editor = editingUser.get(0).getFullName();
			}
		}
		return editor;
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/copy/{reportId}/to/{repositoryIdDestination}", method = RequestMethod.GET)
	public CertificationReport copy(@RequestHeader("Authorization") String authHeader, 
			@PathVariable(name = "reportId") String reportId,
			@PathVariable(name = "repositoryIdDestination") String repositoryIdDestination) {
		
		CertificationReport result = null;
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		
		// Double check on API side only administrators or EDITOR can create a new report
		Repository repo = repositoryDao.findByIdAndUserId(repositoryIdDestination, loggedUser.getUserId());
		if (null != repo) {
			for( RepositoryUser user : repo.getUsers()) {
				if(StringUtils.equals(user.getId(),loggedUser.getUserId())) {
					if(!StringUtils.equals(Roles.EDITOR, user.getRole())) {
						throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to access this report");
					}
					break;
				}
			}
		} else if(!loggedUser.isAdmin()) {
			LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), repositoryIdDestination));
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to access this report");
		}
		
		CertificationReport originalReport = certificationReportDao.findById(reportId);
		CertificationReport copiedReport = new CertificationReport(originalReport);
		copiedReport.setRepositoryId(repositoryIdDestination);
		copiedReport.setStatus(ReportStatus.IN_PROGRESS);
		copiedReport.setVersion("0.1");
		if(StringUtils.equals(originalReport.getStatus().name(), ReportStatus.RELEASED.name())) {
			if(originalReport.getVersion().contains(".")) {
				String[] version = originalReport.getVersion().split(Pattern.quote("."));
				if(StringUtils.isNumeric(version[0])) {
					Integer newVersionDigit = Integer.parseInt(version[0]) + 1;
					copiedReport.setVersion(newVersionDigit.toString().concat(".0"));
				}
				
			}
		}
		result = certificationReportDao.save(copiedReport);
		
		File workDirectory = new File(temporaryFolderName);
		if (workDirectory.exists() == false) {
			workDirectory.mkdirs();
		}
		File localFolder = new File(workDirectory, UUID.randomUUID().toString());
		localFolder.mkdirs();
		ftpClient.copyFiles(localFolder, reportId, copiedReport.getId());
		return result;
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public CertificationReport saveJson(@RequestHeader("Authorization") String authHeader, 
			@RequestBody CertificationReport certificationReport,
			@RequestParam String language) {
		CertificationReport result = null;
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		
		checkStatus(certificationReport.getId());
		
		if (loggedUser.isAdmin()) {
			checkNotifications(certificationReport);
			result = certificationReportDao.save(certificationReport);
		} else {
			Repository repo = repositoryDao.findByIdAndUserId(certificationReport.getRepositoryId(), loggedUser.getUserId());
			if (null != repo) {
				if(repo.getUsers().stream().anyMatch(
						s -> StringUtils.equals(s.getId(),loggedUser.getUserId()) 
						&& !StringUtils.equals(s.getRole(), Roles.EDITOR))) {
					LOG.error(String.format("Le user %s is not MANAGER of the repository id %s. He cannot edit this report", loggedUser.getUserId(), certificationReport.getRepositoryId()));
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to edit this report");
				} else {
					checkNotifications(certificationReport);
					result = certificationReportDao.save(certificationReport);
				}
			} else {
				LOG.error(String.format("Le user %s does not own the repository id %s. He cannot edit this report", loggedUser.getUserId(), certificationReport.getRepositoryId()));
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to edit this report");
			}
		}

		
		return result;
	}
	
	/**
	 * Check if a notification has to be sent:
	 * <li>a report has been validated. All the repository users has be to notified</li>
	 * <li>a report has got a new version. All the repository users has be to notified</li>
	 * @param certificationReportToSave current certification report
	 * @param repositoryName 
	 * @param messages i18n bundle
	 */
	private void checkNotifications(CertificationReport certificationReportToSave) {
		if(certificationReportToSave.getId() != null) {
			CertificationReport reportInDB = certificationReportDao.findById(certificationReportToSave.getId());
			if(reportInDB != null) {
				if(!StringUtils.equals(certificationReportToSave.getStatus().name(), reportInDB.getStatus().name()) &&
						StringUtils.equals(certificationReportToSave.getStatus().name(), ReportStatus.RELEASED.name())) {

					// notification the report has been validated
					notificationUtils.buildNotificationMadatory(certificationReportToSave.getRepositoryId(), 
							appConfig.getReportValidationNotificationSubject(),
							appConfig.getReportValidationNotificationFrenchContent(), 
							appConfig.getReportValidationNotificationEnglishContent(), null);

				} else if(!StringUtils.equals(certificationReportToSave.getVersion(), reportInDB.getVersion())) {
					
					// notification new version
					notificationUtils.buildNotificationMadatory(certificationReportToSave.getRepositoryId(), appConfig.getReportNewVersionNotificationSubject(),
							appConfig.getReportNewVersionNotificationFrenchContent(), 
							appConfig.getReportNewVersionNotificationEnglishContent(), null);
					
				}
			}
		}
	}

	/**
	 * report with released status cannot be updated or deleted
	 * @param id report id
	 * @throws BusinessException 
	 */
	private void checkStatus(String id) throws BusinessException {
		if(id!= null) {
			CertificationReport report = certificationReportDao.findById(id);
			if(report != null && StringUtils.equals(ReportStatus.RELEASED.name(), report.getStatus().name())) {
				throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, 
						String.format("The report with id %s has been released it cannot be modified or deleted", id));
			}
		}
	}

	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public void delete(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "id") String  id) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		
		checkStatus(id);
		
		if (loggedUser.isAdmin()) {
			certificationReportDao.delete(id);
			ftpClient.deleteAllFilesInFolder(id);
		} else {
			CertificationReport report = certificationReportDao.findById(id);
			if(report != null ) {
				Repository repo = repositoryDao.findByIdAndUserId(report.getRepositoryId(), loggedUser.getUserId());
				if (null != repo) {
					if(repo.getUsers().stream().anyMatch(
							s -> StringUtils.equals(s.getId(),loggedUser.getUserId()) 
							&& StringUtils.equals(s.getRole(),"READER"))) {
						LOG.error(String.format("Le user %s is not MANAGER of the repository id %s. He cannot read delete a report", loggedUser.getUserId(), report.getRepositoryId()));
						throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to delete this report");
					} else {
						certificationReportDao.delete(id);
						ftpClient.deleteAllFilesInFolder(id);
					}
				} else {
					LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), report.getRepositoryId()));
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to delete this report");
				}
			}
		}
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getComments/{reportId}", method = RequestMethod.GET)
	public List<RequirementCommentsDto> getComments(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "reportId") String id) {
		return commentsDao.getCommentsByReportId(id);
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getCommentsByUserid/{userId}", method = RequestMethod.GET)
	public List<RequirementCommentsDto> getCommentsByUserid(@RequestHeader("Authorization") String authHeader, 
			@PathVariable(name = "userId") String id) {
		return commentsDao.getCommentsByUserId(id);
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/saveComments", method = RequestMethod.POST)
	public RequirementComments saveComments(@RequestHeader("Authorization") String authHeader, 
			@RequestParam String reportId,
			@RequestParam String repositoryId,
			@RequestParam String requirementCode,
			@RequestParam String language,
			@RequestBody List<Comment> comments) {
		RequirementComments result = commentsDao.getCommentsByReportIdAndRequirementCode(reportId, requirementCode);
		if(result == null) {
			result = new RequirementComments();
			result.setReportId(reportId);
			result.setItemCode(requirementCode);
		}
		result.setComments(comments);
		RequirementComments savedComments = commentsDao.save(result);
		Comment latestComment = comments.get(comments.size()-1);
		//ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale(language));
		Optional<Profile> commentEditor = profileDao.findById(latestComment.getUserId());
		String postedComment = "";
		if(commentEditor.isPresent()) {
			postedComment = "@".concat(commentEditor.get().getName()).concat(": ");
		}
		postedComment = postedComment.concat(latestComment.getText());

		notificationUtils.buildNotificationCheckUserPreference(repositoryId, appConfig.getNewCommentNotificationSubject(), 
				appConfig.getNewCommentNotificationFrenchContent(), 
				appConfig.getNewCommentNotificationEnglishContent(), postedComment);
		return savedComments;
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getCertificationReportTemplate", method = RequestMethod.GET)
	public CertificationTemplate getCertificationReportTemplate(
				@RequestHeader("Authorization") String authHeader, 
				@RequestParam String name) {
		return certificationReportTemplateDao.getCertificationReportTemplate(name);
	}
	
	/**
	 * 
	 * @param response
	 * @param authHeader
	 * @param reportId
	 * @param language
	 * @param format pdf, jspn and xml
	 * @param attachments true if attachments must be export then the result will be a zip file
	 * @param comments true if comments must be exported
	 * @param uploadedFile used to receive radar chart image from UI
	 * @return file in byte array
	 */
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/download", method = RequestMethod.POST, consumes = "multipart/form-data")
	public void download(
			HttpServletResponse response,
			@RequestHeader("Authorization") String authHeader, 
			@RequestParam String reportId,
			@RequestParam String language,
			@RequestParam String format,
			@RequestParam String attachments,
			@RequestParam String comments,
			@RequestParam("radar") MultipartFile uploadedFile) {
		
		try {
			File workDirectory = new File(temporaryFolderName);
			if (workDirectory.exists() == false) {
				workDirectory.mkdirs();
			}
			File localFolder = new File(workDirectory, UUID.randomUUID().toString());
			localFolder.mkdirs();
			
			ApplicationUser loggedUser = LoginUtils.getLoggedUser();
			Report printableReport = getFullReportInformation(loggedUser, reportId, language, Boolean.parseBoolean(comments));
			String fileName = printableReport.getTitle();
			fileName = fileName.replace(" ", "_");
			fileName = Normalizer
			           .normalize(fileName, Normalizer.Form.NFD)
			           .replaceAll("[^\\p{ASCII}]", "");;
			Path filePath = null;
			
			// report and attachments have to be copied into the localFolder
			if(StringUtils.equalsIgnoreCase("PDF", format)) {
				fileName = fileName.concat(".pdf");
				byte[] radarImage = null;
				if (uploadedFile != null) {
					radarImage = uploadedFile.getBytes();
				}
				byte[] content = pdfPrinter.print(language, printableReport, radarImage);
				filePath = Paths.get(localFolder.getAbsolutePath().concat("/").concat(fileName));
				Files.write(filePath, content);
			} else {
				
				ObjectMapper mapper = null;
				if(StringUtils.equalsIgnoreCase("JSON", format)) {
					mapper = new ObjectMapper();
					fileName = fileName.concat(".json");
				} else {
					mapper = new XmlMapper();
					fileName = fileName.concat(".xml");
				}
				String content = mapper.writeValueAsString(printableReport);
				filePath = Paths.get(localFolder.getAbsolutePath().concat("/").concat(fileName));
				Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
			}
			
			if(Boolean.parseBoolean(attachments)) {
				
				ftpClient.downloadFiles(localFolder, reportId, new DomainFilter());
				
				String zipFileName = printableReport.getTitle().concat(".zip");
				File zipFile = new File(workDirectory, zipFileName);
				ZipUtil.pack(localFolder, zipFile);
				Path p = zipFile.toPath();
				InputStream is = Files.newInputStream(p);
				Files.delete(p);
				IOUtils.copyLarge(is, response.getOutputStream());
				response.setContentType(MimeTypeUtils.getMimeType(zipFileName));
			} else {
				InputStream is = Files.newInputStream(filePath);
				IOUtils.copyLarge(is, response.getOutputStream());
				response.setContentType(MimeTypeUtils.getMimeType(fileName));
			}
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}

	/**
	 * Create a PrintableReport from report and template mongoDB collection and scan FTP server for attachments
	 * @param loggedUser
	 * @param reportId
	 * @param language
	 * @param bisCommentsRequested 
	 * @param service
	 * @return PrintableReport
	 */
	private Report getFullReportInformation(ApplicationUser loggedUser, String reportId, String language, boolean isCommentsRequested) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        
		Report printableReport = new Report();
		
		CertificationReport report = certificationReportDao.findById(reportId);

		if(report != null) {
			
			List<TemplateName> templatesName = certificationReportTemplateDao.getTemplateNameList();
			
			// main report information
			printableReport.setStatus(messages.getString(report.getStatus().toString()));
			printableReport.setUpdateDate(report.getUpdateDate());
			printableReport.setVersion(report.getVersion());
			
			// Get repository name and check access in mean time
			if(report != null ) {
				Repository repo = null;
				if (!loggedUser.isAdmin()) {
					repo = repositoryDao.findByIdAndUserId(report.getRepositoryId(), loggedUser.getUserId());
				} else {
					repo = repositoryDao.findById(report.getRepositoryId());
				}
				if (null != repo) {
					if(repo.getAffiliationId() != null) {
						Affiliation affiliation = affiliationDao.findById(repo.getAffiliationId());
						printableReport.setAffiliation(new AffiliationDto(affiliation));
					}
					for(TemplateName templateName :templatesName) {
						if(StringUtils.equals(report.getTemplateId(), templateName.getId())) {
							printableReport.setTitle(repo.getName().concat(" ").concat(templateName.getName()));
							break;
						}
					}
				} else {
					LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), report.getRepositoryId()));
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "You do not have rights to access this report");
				}
			}
			
			// Get template information and loop on requirements
			printableReport.setRequirements(getRequirementInformation(report, language, isCommentsRequested));
			return printableReport;
		} else {
			return null;
		}

	}

	/**
	 * Get template information and loop on requirements
	 * @param report object found in mongoDB
	 * @param language input parameter
	 * @param isCommentsRequested 
	 * @param service input parameter
	 * @return List<PrintableRequirement>
	 */
	private List<Requirement> getRequirementInformation(
			CertificationReport report, 
			String language, boolean isCommentsRequested) {
		CertificationTemplate template = certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateId());
		Map<String, LevelTemplate> levels = getLevelMap(template.getLevels());
		Map<String, RequirementTemplate> requirements = getRequirementMap(template.getRequirements());
		List<Requirement> printableRequiments = new ArrayList<>();
		
		Map<String, List<String>> attachments = ftpClient.listFiles(report.getId());
		

		Map<String, List<CommentExport>> map = new HashMap<String, List<CommentExport>>();
		if(isCommentsRequested) {
			List<RequirementCommentsDto> comments = commentsDao.getCommentsByReportId(report.getId());
			if(comments != null) {
				 for(RequirementCommentsDto commentsByRequirement : comments) {
					 List<CommentExport> commentsExportList = new ArrayList<CommentExport>();
					 for (CommentDto commentItem : commentsByRequirement.getComments()) {
						 commentsExportList.add(new CommentExport(commentItem, language));
					 }
					 map.put(commentsByRequirement.getItemCode(), commentsExportList);
				 }
			}
		}
		
		for (CertificationItem r : report.getItems()) {
			Requirement printableRequirement = new Requirement();
			printableRequirement.setCode(r.getCode());
			printableRequirement.setLevel(r.getLevel());
			printableRequirement.setRequirementLabel(requirements.get(r.getCode()).getRequirement());
			if(r.getLevel() != null) {
				printableRequirement.setLevelLabel(levels.get(r.getLevel()).getLabel());
			}
			printableRequirement.setResponse(r.getResponse());
			
			if(map != null && map.containsKey(r.getCode())) {
				printableRequirement.setComments(map.get(r.getCode()));
			}
			
			
			printableRequiments.add(printableRequirement);
			
			if(attachments != null) {
				printableRequirement.setAttachments(attachments.get(r.getCode()));
			}

		}
		return printableRequiments;
	}

	/**
	 * Concert List<LevelTemplate> into Map<String, LevelTemplate>
	 * @param levels List<LevelTemplate> 
	 * @return Map<String, LevelTemplate>
	 */
	private Map<String, LevelTemplate> getLevelMap(List<LevelTemplate> levels) {
		Map<String, LevelTemplate> mapLevels = null;
		if(levels != null && levels.size() > 0) {
			mapLevels = new HashMap<>();
			for(LevelTemplate level : levels) {
				mapLevels.put(level.getCode(), level);
			}
		}
		return mapLevels;
	}
	
	/**
	 * Concert List<RequirementTemplate> into Map<String, RequirementTemplate>
	 * @param requirements List<RequirementTemplate> 
	 * @return Map<String, RequirementTemplate>
	 */
	private Map<String, RequirementTemplate> getRequirementMap(List<RequirementTemplate> requirements) {
		Map<String, RequirementTemplate> mapRequirements = null;
		if(requirements != null && requirements.size() > 0) {
			mapRequirements = new HashMap<>();
			for(RequirementTemplate requirement : requirements) {
				mapRequirements.put(requirement.getCode(), requirement);
			}
		}
		return mapRequirements;
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/updateConnectedUser", method = RequestMethod.POST)
	public void updateConnectedUser(@RequestHeader("Authorization") String authHeader, 
			@RequestParam String reportId, @RequestParam String userId, @RequestParam String userName) {
		connectedUserDao.updateCache(reportId, userId, userName);
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/listConnectedUser", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, List<ConnectedUser>> listConnectedUser(@RequestHeader("Authorization") String authHeader,
			@RequestParam List<String> reportIdList, @RequestParam String userId) {
		
		Map<String, List<ConnectedUser>> result = new HashMap<String, List<ConnectedUser>>();
		
		List<ConnectedUser> users = new ArrayList<ConnectedUser>();
		for(String reportId : reportIdList) {
			List<ConnectedUser> res = connectedUserDao.getConnectedUsersByReportId(reportId);
			if(res != null && res.size() > 0) {
				users.addAll(res);
				result.put(reportId, users.stream().filter(u -> !StringUtils.equals(u.getUserId(), userId)).
					    collect(Collectors.toList()));
			}
		}
		return result;
	}

}
