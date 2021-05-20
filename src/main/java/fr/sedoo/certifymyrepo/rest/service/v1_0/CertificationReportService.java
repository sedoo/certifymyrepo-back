package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.zeroturnaround.zip.ZipUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.dao.CommentsDao;
import fr.sedoo.certifymyrepo.rest.dao.RepositoryDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.Comment;
import fr.sedoo.certifymyrepo.rest.domain.MyReport;
import fr.sedoo.certifymyrepo.rest.domain.MyReports;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;
import fr.sedoo.certifymyrepo.rest.domain.Repository;
import fr.sedoo.certifymyrepo.rest.domain.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.LevelTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.TemplateName;
import fr.sedoo.certifymyrepo.rest.dto.CertificationItemDto;
import fr.sedoo.certifymyrepo.rest.dto.ReportDto;
import fr.sedoo.certifymyrepo.rest.export.CommentDto;
import fr.sedoo.certifymyrepo.rest.export.PdfPrinter;
import fr.sedoo.certifymyrepo.rest.export.Report;
import fr.sedoo.certifymyrepo.rest.export.Requirement;
import fr.sedoo.certifymyrepo.rest.ftp.DomainFilter;
import fr.sedoo.certifymyrepo.rest.ftp.SimpleFtpClient;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.ForbiddenException;
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
	private SimpleFtpClient ftpClient;
	
	@Autowired
	private CertificationReportTemplateDao certificationReportTemplateDao;
	
	@Autowired
	private PdfPrinter pdfPrinter;
	
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
			ReportDto reportDto = new ReportDto(report);
			// For a given repository, reports can be made from different template
			CertificationTemplate template = certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateId());
			// Set the maximum value of radar chart yaxis
			reportDto.setLevelMaxValue(template.getLevels().size()-1);
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
				throw new ForbiddenException();
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
					throw new ForbiddenException();
				}
			}
		}

		result.setRequirementComments(commentsDao.getCommentsByReportId(id));
		result.setTemplate(certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateId()));
		if(ftpClient.checkDirectoryExistance(id)) {
			result.setAttachments(ftpClient.listFiles(id));
		}
		return result;
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public CertificationReport saveJson(@RequestHeader("Authorization") String authHeader, @RequestBody CertificationReport certificationReport) {
		CertificationReport result = null;
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		
		checkStatus(certificationReport.getId());
		
		if (loggedUser.isAdmin()) {
			result = certificationReportDao.save(certificationReport);
		} else {
			Repository repo = repositoryDao.findByIdAndUserId(certificationReport.getRepositoryId(), loggedUser.getUserId());
			if (null != repo) {
				if(repo.getUsers().stream().anyMatch(
						s -> StringUtils.equals(s.getId(),loggedUser.getUserId()) 
						&& !StringUtils.equals(s.getRole(), Roles.EDITOR))) {
					LOG.error(String.format("Le user %s is not MANAGER of the repository id %s. He cannot edit this report", loggedUser.getUserId(), certificationReport.getRepositoryId()));
					throw new ForbiddenException();
				} else {
					result = certificationReportDao.save(certificationReport);
				}
			} else {
				LOG.error(String.format("Le user %s does not own the repository id %s. He cannot edit this report", loggedUser.getUserId(), certificationReport.getRepositoryId()));
				throw new ForbiddenException();
			}
		}
		return result;
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
				throw new BusinessException(String.format("The report with id %s has been released it cannot be modified or deleted", id));
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
		} else {
			CertificationReport report = certificationReportDao.findById(id);
			if(report != null ) {
				Repository repo = repositoryDao.findByIdAndUserId(report.getRepositoryId(), loggedUser.getUserId());
				if (null != repo) {
					if(repo.getUsers().stream().anyMatch(
							s -> StringUtils.equals(s.getId(),loggedUser.getUserId()) 
							&& StringUtils.equals(s.getRole(),"READER"))) {
						LOG.error(String.format("Le user %s is not MANAGER of the repository id %s. He cannot read delete a report", loggedUser.getUserId(), report.getRepositoryId()));
						throw new ForbiddenException();
					} else {
						certificationReportDao.delete(id);
					}
				} else {
					LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), report.getRepositoryId()));
					throw new ForbiddenException();
				}
			}
		}
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getComments/{reportId}", method = RequestMethod.GET)
	public List<RequirementComments> getComments(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "reportId") String id) {
		return commentsDao.getCommentsByReportId(id);
	}
	
	@RequestMapping(value = "/getCommentsByUserid/{userId}", method = RequestMethod.GET)
	public List<RequirementComments> getCommentsByUserid(@PathVariable(name = "userId") String id) {
		return commentsDao.getCommentsByUserId(id);
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/saveComments", method = RequestMethod.POST)
	public RequirementComments saveComments(@RequestHeader("Authorization") String authHeader, 
			@RequestParam String reportId,
			@RequestParam String requirementCode,
			@RequestBody List<Comment> comments) {
		RequirementComments result = commentsDao.getCommentsByReportIdAndRequirementCode(reportId, requirementCode);
		if(result == null) {
			result = new RequirementComments();
			result.setReportId(reportId);
			result.setItemCode(requirementCode);
		}
		result.setComments(comments);
		return commentsDao.save(result);
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
				
				ftpClient.downloadContent(localFolder, reportId, new DomainFilter());
				
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
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
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
					for(TemplateName templateName :templatesName) {
						if(StringUtils.equals(report.getTemplateId(), templateName.getId())) {
							printableReport.setTitle(repo.getName().concat(" ").concat(templateName.getName()));
							break;
						}
					}
				} else {
					LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), report.getRepositoryId()));
					throw new ForbiddenException();
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
		
		Map<String, List<String>> attachments = null;
		if(ftpClient.checkDirectoryExistance(report.getId())) {
			attachments = ftpClient.listFiles(report.getId());
		}
		
		List<RequirementComments> comments = commentsDao.getCommentsByReportId(report.getId());
		Map<String, List<CommentDto>> map = new HashMap<String, List<CommentDto>>();
		if(comments != null) {
			 for(RequirementComments commentsByRequirement : comments) {
				 List<CommentDto> commentsDto = new ArrayList<CommentDto>();
				 for (Comment commentItem : commentsByRequirement.getComments()) {
					 commentsDto.add(new CommentDto(commentItem));
				 }
				 map.put(commentsByRequirement.getItemCode(), commentsDto);
			 }
		}
		
		for (CertificationItem r : report.getItems()) {
			Requirement printableRequirement = new Requirement();
			if(StringUtils.equals("fr", language)) {
				printableRequirement.setRequirement(requirements.get(r.getCode()).getRequirement().getFr());
				if(r.getLevel() != null) {
					printableRequirement.setLevelLabel(levels.get(r.getLevel()).getLabel().getFr());
				}
			} else {
				printableRequirement.setRequirement(requirements.get(r.getCode()).getRequirement().getEn());
				if(r.getLevel() != null) {
					printableRequirement.setLevelLabel(levels.get(r.getLevel()).getLabel().getEn());
				}
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

}
