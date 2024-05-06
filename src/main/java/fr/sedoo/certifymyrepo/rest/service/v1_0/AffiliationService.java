package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.dao.AffiliationDao;
import fr.sedoo.certifymyrepo.rest.domain.Affiliation;
import fr.sedoo.certifymyrepo.rest.dto.AffiliationDto;

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

	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public List<AffiliationDto> list(HttpServletRequest request) {

		List<AffiliationDto> result = new ArrayList<AffiliationDto>();

		List<Affiliation> aux = affiliationDao.findAll();
		for (Affiliation affiliation : aux) {
			result.add(new AffiliationDto(affiliation));
		}
		Collections.sort(result);
		return result;
	}

	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public Affiliation saveAffiliation(HttpServletRequest request, @RequestBody Affiliation affiliation) {
		return affiliationDao.save(affiliation);
	}
	
	@PreAuthorize("@permissionEvaluator.isAdmin(#request)")
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
	public void delete(HttpServletRequest request, @PathVariable(name = "id") String id) {
		affiliationDao.deleteById(id);
	}

}
