package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import fr.sedoo.certifymyrepo.rest.config.OrcidConfig;
import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.dao.ProfileDao;
import fr.sedoo.certifymyrepo.rest.domain.LoggedUser;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtConfig;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtUtil;
import fr.sedoo.certifymyrepo.rest.filter.jwt.OrcidToken;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;

@RestController
@CrossOrigin
@RequestMapping(value = "/login/v1_0")
public class LoginService {

	public static final String ORCID_KEY = "orcid";
	
	public static final String UUID_KEY = "uuid";
	
	@Autowired
	OrcidConfig orcidConfig;
	
	@Autowired
	AdminDao adminDao;
	
	@Autowired
	JwtConfig jwtConfig;
	
	@Autowired
	ProfileDao profileDao;
	
	@RequestMapping(value="/orcid",method=RequestMethod.POST)
	public LoggedUser orcid(HttpServletResponse response, @RequestParam String code, @RequestParam(name="redirect_uri") String redirectUri) throws Exception{
		
		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);

		WebResource service = client.resource(orcidConfig.getTokenUrl());

		MultivaluedMap<String, String> data = new MultivaluedMapImpl();		
		data.add("client_id", orcidConfig.getClientId());
		data.add("client_secret", orcidConfig.getClientSecret());
		data.add("grant_type", "authorization_code");
		data.add("redirect_uri", redirectUri);
		data.add("code", code);
		
		ClientResponse clientResponse =  service.accept("application/json").post(ClientResponse.class, data);
		
		if (clientResponse.getStatus() == 200){
			OrcidToken orcidToken = clientResponse.getEntity(OrcidToken.class);
			
			LoggedUser user = new LoggedUser();
			Profile profile = profileDao.findByOrcid(orcidToken.getOrcid());
			if(profile == null) {
				profile = new Profile();
				profile.setName(orcidToken.getName());
				profile.setOrcid(orcidToken.getOrcid());
				profile = profileDao.save(profile);
			}
			user.setProfile(profile);
			user.setToken(generateToken(profile.getName(), profile.getId()));
			user.setAdmin(adminDao.isAdmin(profile.getId()));
			user.setSuperAdmin(adminDao.isSuperAdmin(profile.getId()));
			
			return user;
		} else{
			response.setStatus(clientResponse.getStatus());
			return null;
		}
	}
	
	@Deprecated
	private String generateToken(OrcidToken orcidToken) throws Exception{
		Map<String, String> infos = new HashMap<>();
		infos.put(ORCID_KEY, orcidToken.getOrcid());

		String token = JwtUtil.generateToken(orcidToken.getName(), jwtConfig.getSigningKey(), jwtConfig.getTokenValidity(), null, infos);
		return token;
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
