package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.dto.ContactDto;
import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.notification.EmailSender;

@RestController
@CrossOrigin
@RequestMapping(value = "/admin/v1_0")
public class AdminService {

	@Autowired
	private AdminDao adminDao;
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private EmailSender emailSender;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}
	
	@PreAuthorize("@permissionEvaluator.isAdmin(#request)")
	@RequestMapping(value = "/save/{userId}", method = RequestMethod.POST)
	public String save(HttpServletRequest request, @PathVariable(name = "userId") String  userId) {
		String adminId = null;
		Optional<Profile> user = profileDao.findById(userId);
		if(user.isPresent()) {
			Admin admin = new Admin();
			admin.setName(user.get().getName());
			admin.setUserId(userId);
			Admin result = adminDao.save(admin);
			if(result !=null) {
				adminId = result.getId();
			}
		} else {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, String.format("User % does not exist", userId));
		}
		return adminId;
	}
	
	@PreAuthorize("@permissionEvaluator.isAdmin(#request)")
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public boolean delete(HttpServletRequest request, @PathVariable(name = "id") String  id) {
		adminDao.delete(id);
		return true;
	}
	
	@PreAuthorize("@permissionEvaluator.isAdmin(#request)")
	@RequestMapping(value = "/listAllUsers", method = RequestMethod.GET)
	public List<ProfileDto> listAll(HttpServletRequest request) {
		List<ProfileDto> result = new ArrayList<>();
		List<Profile> usersProfile = profileDao.findAll();
		Map<String, Admin> mapAdmin = getAllAdmin();
		if(usersProfile != null) {
			for( Profile userProfile : usersProfile) {
				ProfileDto user = new ProfileDto();
				user.setId(userProfile.getId());
				user.setEmail(userProfile.getEmail());
				user.setName(userProfile.getName());
				user.setOrcid(userProfile.getOrcid());
				Admin admin = mapAdmin.get(userProfile.getId());
				if(admin != null && !admin.isSuperAdmin()){
					user.setAdminId(admin.getId());
				}
				// only users and admin (COSO) are visible
				// SuperAdmin are hidden
				if(admin == null || !admin.isSuperAdmin()) {
					result.add(user);
				}
			}
		}
		return result;
	}

	private Map<String, Admin> getAllAdmin() {
		Map<String, Admin> map = new HashMap<>();
		List<Admin> list = adminDao.findAll();
		if(list != null) {
			for(Admin admin : list) {
				map.put(admin.getUserId(), admin);
			}
		}
		return map;
	}
	
	@RequestMapping(value = "/contact", method = RequestMethod.POST)
	public boolean contact(@RequestBody ContactDto contact) {
		Set<String> functionalAdminEmails = new HashSet<String>();
		List<Admin> functionalAdmins = adminDao.findAllFunctaionalAdmin();
		for(Admin functionalAdmin : functionalAdmins) {
			Optional<fr.sedoo.certifymyrepo.rest.domain.Profile> userProfile = profileDao.findById(functionalAdmin.getUserId());
			if(userProfile.isPresent() && StringUtils.isNotBlank(userProfile.get().getEmail())) {
				functionalAdminEmails.add(userProfile.get().getEmail());
			}
		}
		contact.setTo(functionalAdminEmails);
		return emailSender.sendNotification(contact);
	}

}
