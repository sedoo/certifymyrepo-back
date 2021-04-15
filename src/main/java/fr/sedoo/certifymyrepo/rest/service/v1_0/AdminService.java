package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.Admin;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.dto.User;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;
import io.swagger.annotations.ApiOperation;

@RestController
@CrossOrigin
@RequestMapping(value = "/admin/v1_0")
public class AdminService {

	@Autowired
	AdminDao adminDao;
	
	@Autowired
	ProfileDao profileDao;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}
	
	@Secured({Roles.AUTHORITY_ADMIN})
	@RequestMapping(value = "/save/{userId}", method = RequestMethod.POST)
	public String save(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "userId") String  userId) {
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
			throw new BusinessException(String.format("User % does not exist", userId));
		}
		return adminId;
	}
	
	@ApiOperation(value = "Delete an admin by Id")
	@Secured({Roles.AUTHORITY_ADMIN})
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public boolean delete(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "id") String  id) {
		adminDao.delete(id);
		return true;
	}
	
	@Secured({Roles.AUTHORITY_ADMIN})
	@RequestMapping(value = "/listAllUsers", method = RequestMethod.GET)
	public List<User> listAll(@RequestHeader("Authorization") String authHeader) {
		List<User> result = new ArrayList<>();
		List<Profile> usersProfile = profileDao.findAll();
		Map<String, Admin> mapAdmin = getAllAdmin();
		if(usersProfile != null) {
			for( Profile userProfile : usersProfile) {
				User user = new User();
				user.setUserId(userProfile.getId());
				user.setEmail(userProfile.getEmail());
				user.setName(userProfile.getName());
				Admin admin = mapAdmin.get(userProfile.getId());
				if(admin != null && !admin.isSuperAdmin()){
					user.setAdminId(admin.getId());
					user.setAdmin(true);
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

}
