package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import fr.sedoo.certifymyrepo.rest.config.OrcidConfig;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.OrcidDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.LoggedUser;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtConfig;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtUtil;
import fr.sedoo.certifymyrepo.rest.filter.jwt.OrcidToken;
import fr.sedoo.certifymyrepo.rest.filter.jwt.ShibbolethToken;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;

@RestController
@CrossOrigin
@RequestMapping(value = "/login/v1_0")
public class LoginService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);

	public static final String ORCID_KEY = "orcid";
	
	public static final String UUID_KEY = "uuid";
	
	public static final String SHIBBOLETH_AUTHENTIFICATION_TYPE = "shibb";
	
	@Autowired
	private OrcidConfig orcidConfig;
	
	@Autowired
	private AdminDao adminDao;
	
	@Autowired
	private JwtConfig jwtConfig;
	
	@Autowired
	private ProfileDao profileDao;
	
	@Autowired
	private OrcidDao orcidDao;
	
	@Value("${shibboleth.url}")
	private String shibbolethUrl;
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public LoggedUser login(HttpServletResponse response, 
			@RequestParam String code, 
			@RequestParam(required = false) String type, 
			@RequestParam(name="redirect_uri") String redirectUri) throws Exception{
		
		LoggedUser loggedUser = null;
		
		RestTemplate restTemplate = new RestTemplate();
		if(StringUtils.equals(type, SHIBBOLETH_AUTHENTIFICATION_TYPE)) {
			ResponseEntity<ShibbolethToken> clientResponse = restTemplate.getForEntity(shibbolethUrl.concat("/shibboleth/userbycode?code=").concat(code), ShibbolethToken.class);
			if (clientResponse.getStatusCode().is2xxSuccessful()){
				ShibbolethToken shibbolethToken = clientResponse.getBody();
				// An email can have upper case letter it must be stored  in  lower case
				String email = null;
				if(shibbolethToken.getMail() != null) {
					email = shibbolethToken.getMail().toLowerCase();
				}
				loggedUser = this.getLoggedUser(null, email, shibbolethToken.getGivenname().concat(" ").concat(shibbolethToken.getName()));
			} else {
				response.setStatus(clientResponse.getStatusCodeValue());
			}
		} else {
			
	        HttpHeaders headers = new HttpHeaders();
	        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	        
	        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();	
			requestBody.add("client_id", orcidConfig.getClientId());
			requestBody.add("client_secret", orcidConfig.getClientSecret());
			requestBody.add("grant_type", "authorization_code");
			requestBody.add("redirect_uri", redirectUri);
			requestBody.add("code", code);
			
			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
			ResponseEntity<OrcidToken> postResponse = restTemplate.postForEntity(orcidConfig.getTokenUrl(), request, OrcidToken.class);
			if (postResponse.getStatusCode().is2xxSuccessful()){
				OrcidToken orcidToken = postResponse.getBody();
				ProfileDto orcidProfile = orcidDao.getUserInfoByOrcid(orcidToken.getOrcid());
				// An email can have upper case letter it must be stored  in  lower case
				String email = null;
				if(orcidProfile.getEmail() != null) {
					email = orcidProfile.getEmail().toLowerCase();
				}
				loggedUser = this.getLoggedUser(orcidToken.getOrcid(), email, orcidToken.getName());
			} else {
				response.setStatus(postResponse.getStatusCodeValue());
			}
		}
		return loggedUser;
	}
	
	@RequestMapping(value="/DiscoFeed",method=RequestMethod.GET)
	public String discoFeed(HttpServletResponse response) {
		String result = null;
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> clientResponse = restTemplate.getForEntity(shibbolethUrl.concat("/Shibboleth.sso/DiscoFeed"), String.class);
		

		if (clientResponse.getStatusCode().is2xxSuccessful()){
			result = clientResponse.getBody();
		} else {
			LOG.error(clientResponse.toString());
			response.setStatus(clientResponse.getStatusCodeValue());
		}
		return result;
	}
	
	private LoggedUser getLoggedUser(String orcid, String email, String name) throws Exception {
		LoggedUser user = new LoggedUser();
		Profile profile = null;
		if(orcid == null) {
			profile = profileDao.findByEmail(email);
		} else {
			profile = profileDao.findByOrcid(orcid);
		}
		// First login, a profile has to be created in Mongo DB
		if(profile == null) {
			profile = new Profile();
			profile.setName(name);
			profile.setEmail(email);
			profile.setOrcid(orcid);
			profile = profileDao.save(profile);
		// Check if the user name has been update on ORCID or Renater
		} else if(!StringUtils.equals(profile.getName(), name) || ( email != null && !StringUtils.equals(profile.getEmail(), email))) {
			profile.setName(name);
			profile.setEmail(email);
			profile = profileDao.save(profile);	
		}
		user.setProfile(profile);
		user.setToken(generateToken(profile.getName(), profile.getId()));
		user.setAdmin(adminDao.isAdmin(profile.getId()));
		user.setSuperAdmin(adminDao.isSuperAdmin(profile.getId()));
		
		return user;
	}
	
	private String generateToken(String name, String uuid) throws Exception{
		Map<String, String> infos = new HashMap<>();
		infos.put(UUID_KEY, uuid);

		String token = JwtUtil.generateToken(name, jwtConfig.getSigningKey(), jwtConfig.getTokenValidity(), null, infos);
		return token;
	}
	
	@Secured({ Roles.AUTHORITY_USER })
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public String refreshToken(@RequestHeader("Authorization") String authHeader, HttpServletResponse response) {
        return StringUtils.trimToEmpty(response.getHeader(JwtUtil.AUTH_HEADER));
    }
	
}
