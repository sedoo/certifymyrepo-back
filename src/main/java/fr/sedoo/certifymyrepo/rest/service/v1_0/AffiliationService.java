package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.dao.AffiliationDao;
import fr.sedoo.certifymyrepo.rest.domain.Affiliation;
import fr.sedoo.certifymyrepo.rest.dto.AffiliationDto;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;

@RestController
@CrossOrigin
@RequestMapping(value = "/myaffi/v1_0")
public class AffiliationService {

	@Autowired
	AffiliationDao affiliationDao;

	@RequestMapping(value = "/isalive", method = RequestMethod.GET)
	public String isalive() {
		return "yes";
	}

	@Secured({ Roles.AUTHORITY_USER })
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<AffiliationDto> list() {

		List<AffiliationDto> result = new ArrayList<AffiliationDto>();

		List<Affiliation> aux = affiliationDao.findAll();
		for (Affiliation affiliation : aux) {
			result.add(new AffiliationDto(affiliation));
		}
		Collections.sort(result);
		return result;
	}

	@Secured({ Roles.AUTHORITY_USER })
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Affiliation saveAffiliation(@RequestBody Affiliation affiliation) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		if (loggedUser == null) {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Not logged");
		} else {
			return affiliationDao.save(affiliation);
		}
	}
	
	@Secured({ Roles.AUTHORITY_ADMIN })
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public void delete(@RequestHeader("Authorization") String authHeader, @PathVariable(name = "id") String id) {
		ApplicationUser loggedUser = LoginUtils.getLoggedUser();
		if (loggedUser == null) {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "Not logged");
		} else {
			affiliationDao.deleteById(id);
		}
	}

}
