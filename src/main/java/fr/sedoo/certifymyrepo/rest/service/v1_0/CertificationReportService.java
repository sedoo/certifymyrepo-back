package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
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
import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.LevelTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryUser;
import fr.sedoo.certifymyrepo.rest.ftp.SimpleFtpClient;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.print.PdfPrinter;
import fr.sedoo.certifymyrepo.rest.print.PrintableReport;
import fr.sedoo.certifymyrepo.rest.print.PrintableRequirement;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;
import fr.sedoo.certifymyrepo.rest.service.v1_0.exception.ForbiddenException;

@RestController
@CrossOrigin
@RequestMapping(value = "/certificationReport/v1_0")
public class CertificationReportService {

	private static final Logger LOG = LoggerFactory.getLogger(CertificationReportService.class);

	@Autowired
	CertificationReportDao certificationReportDao;
	
	@Autowired
	RepositoryDao repositoryDao;
	
	@Autowired
	CommentsDao commentsDao;
	
	@Autowired
	SimpleFtpClient ftpClient;
	
	@Autowired
	CertificationReportTemplateDao certificationReportTemplateDao;
	
	@Autowired
	PdfPrinter pdfPrinter;
	
	@Autowired
	ApplicationConfig config;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/listByRepositoryId/{repositoryId}", method = RequestMethod.GET)
	public MyReports listByRepositoryId(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "repositoryId") String repositoryId) {
		MyReports result = new MyReports();
		List<CertificationReport> reports = certificationReportDao.findByRepositoryId(repositoryId);
		result.setReports(reports);
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
		result.setTemplate(certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateName()));
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
			@RequestParam Integer requirementCode,
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
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getPDF", method = RequestMethod.POST, consumes = "multipart/form-data")
	public byte[] getPDF(
				HttpServletResponse response,
				@RequestHeader("Authorization") String authHeader, 
				@RequestParam String reportId,
				@RequestParam String language,
				@RequestParam String service,
				@RequestParam("radar") MultipartFile uploadedFile) {
		byte[] content = null;
		try {
			
			ApplicationUser loggedUser = LoginUtils.getLoggedUser();
			PrintableReport printableReport = getFullReportInformation(loggedUser, reportId, language, service);
			if(printableReport != null) {
				byte[] radarImage = null;
				if (uploadedFile != null) {
					radarImage = uploadedFile.getBytes();
				}
				content = pdfPrinter.print(language, printableReport, radarImage);
			}
		} catch (Exception e) {
			LOG.error("Error with radar image", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		return content;
	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getJSON", method = RequestMethod.GET)
	public PrintableReport getJSON(
				HttpServletResponse response,
				@RequestHeader("Authorization") String authHeader, 
				@RequestParam String reportId,
				@RequestParam String language,
				@RequestParam String service) {

		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		return getFullReportInformation(loggedUser, reportId, language, service);

	}
	
	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(value = "/getXML", method = RequestMethod.GET)
	public String getXML(
				HttpServletResponse response,
				@RequestHeader("Authorization") String authHeader, 
				@RequestParam String reportId,
				@RequestParam String language,
				@RequestParam String service) throws JsonProcessingException {

		try {
			ApplicationUser loggedUser = LoginUtils.getLoggedUser();
			PrintableReport report = getFullReportInformation(loggedUser, reportId, language, service);
			XmlMapper xmlMapper = new XmlMapper();
			return xmlMapper.writeValueAsString(report);
		} catch (JsonProcessingException e) {
			LOG.error("Error XML report", e);
			throw e;
			
		}
	}

	/**
	 * Create a PrintableReport from report and template mongoDB collection and scan FTP server for attachments
	 * @param loggedUser
	 * @param reportId
	 * @param language
	 * @param service
	 * @return PrintableReport
	 */
	private PrintableReport getFullReportInformation(ApplicationUser loggedUser, String reportId, String language, String service) {
		
        Locale locale = new Locale(language);
        ResourceBundle messages = ResourceBundle.getBundle("messages", locale);
        
		PrintableReport printableReport = new PrintableReport();
		
		CertificationReport report = certificationReportDao.findById(reportId);

		if(report != null) {
			
			// main report information 
			printableReport.setStatus(messages.getString(report.getStatus().toString()));
			printableReport.setUpdateDate(report.getUpdateDate());
			printableReport.setVersion(report.getVersion());
			
			// Get repository name and check access in mean time
			if (!loggedUser.isAdmin()) {
				if(report != null ) {
					Repository repo = repositoryDao.findByIdAndUserId(report.getRepositoryId(), loggedUser.getUserId());
					if (null != repo) {
						printableReport.setTitle(repo.getName().concat(" ").concat(report.getTemplateName()));
					} else {
						LOG.error(String.format("Le user %s does not own the repository id %s. He cannot read the reports", loggedUser.getUserId(), report.getRepositoryId()));
						throw new ForbiddenException();
					}
				}
			}
			
			// Get template information and loop on requirements
			printableReport.setRequirements(getRequirementInformation(report, language, service));
			return printableReport;
		} else {
			return null;
		}

	}

	/**
	 * Get template information and loop on requirements
	 * @param report object found in mongoDB
	 * @param language input parameter
	 * @param service input parameter
	 * @return List<PrintableRequirement>
	 */
	private List<PrintableRequirement> getRequirementInformation(
			CertificationReport report, 
			String language,
			String service) {
		CertificationTemplate template = certificationReportTemplateDao.getCertificationReportTemplate(report.getTemplateName());
		Map<String, LevelTemplate> levels = getLevelMap(template.getLevels());
		Map<String, RequirementTemplate> requirements = getRequirementMap(template.getRequirements());
		List<PrintableRequirement> printableRequiments = new ArrayList<>();
		
		Map<String, List<String>> attachments = null;
		if(ftpClient.checkDirectoryExistance(report.getId())) {
			attachments = ftpClient.listFiles(report.getId());
		}
		
		for (CertificationItem r : report.getItems()) {
			PrintableRequirement printableRequirement = new PrintableRequirement();
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
			printableRequiments.add(printableRequirement);
			
			if(attachments != null) {
				List<String> list = attachments.get(r.getCode());
				if(list != null && list.size() > 0) {
					Map<String, String> mapValueUrl = new HashMap<String, String>();
					for(String file : list) {
						mapValueUrl.put(file, String.format("%s/link/%s/%s/%s", service, report.getId(), r.getCode(), file));
					}
					printableRequirement.setAttachments(mapValueUrl);
				}
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
