package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
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
	
	@Value("${shibboleth.url}")
	private String shibbolethUrl;
	
	@RequestMapping(value="/login",method=RequestMethod.POST)
	public LoggedUser login(HttpServletResponse response, 
			@RequestParam String code, 
			@RequestParam String type, 
			@RequestParam(name="redirect_uri") String redirectUri) throws Exception{
		
		LoggedUser loggedUser = null;
		
		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);
		if(StringUtils.equals(type, SHIBBOLETH_AUTHENTIFICATION_TYPE)) {
			WebResource service = client.resource(shibbolethUrl.concat("/shibboleth/userbycode?code=").concat(code));
			ClientResponse clientResponse =  service.accept("application/json").get(ClientResponse.class);
			if (clientResponse.getStatus() == 200){
				ShibbolethToken shibbolethToken = clientResponse.getEntity(ShibbolethToken.class);
				loggedUser = this.getLoggedUser(null, shibbolethToken.getMail(), shibbolethToken.getGivenname().concat(" ").concat(shibbolethToken.getName()));
			} else {
				response.setStatus(clientResponse.getStatus());
			}
		} else {
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
				loggedUser = this.getLoggedUser(orcidToken.getOrcid(), null, orcidToken.getName());
			} else {
				response.setStatus(clientResponse.getStatus());
			}
		}
		return loggedUser;
	}
	
	@RequestMapping(value="/DiscoFeed",method=RequestMethod.GET)
	public String discoFeed(HttpServletResponse response) {
		String result = null;
		ClientConfig config = new DefaultClientConfig();
		config.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
		Client client = Client.create(config);
		WebResource service = client.resource(shibbolethUrl.concat("/Shibboleth.sso/DiscoFeed"));
		ClientResponse clientResponse =  service.accept("application/json").get(ClientResponse.class);
		if (clientResponse.getStatus() == 200){
			InputStream eis = clientResponse.getEntityInputStream();
			try {
				result = IOUtils.toString(eis, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				LOG.error("Error while parsing /Shibboleth.sso/DiscoFeed response", e);
			}
		} else {
			response.setStatus(clientResponse.getStatus());
		}
		return result;
	}
	
	@Deprecated
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
	
	private LoggedUser getLoggedUser(String orcid, String email, String name) throws Exception {
		LoggedUser user = new LoggedUser();
		Profile profile = null;
		if(orcid == null) {
			profile = profileDao.findByEmail(email);
		} else {
			profile = profileDao.findByOrcid(orcid);
		}
		if(profile == null) {
			profile = new Profile();
			profile.setName(name);
			profile.setEmail(email);
			profile.setOrcid(orcid);
			profile = profileDao.save(profile);
		}
		user.setProfile(profile);
		user.setToken(generateToken(profile.getName(), profile.getId()));
		user.setAdmin(adminDao.isAdmin(profile.getId()));
		user.setSuperAdmin(adminDao.isSuperAdmin(profile.getId()));
		
		return user;
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
