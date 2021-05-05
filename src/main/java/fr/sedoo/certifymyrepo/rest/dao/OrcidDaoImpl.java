package fr.sedoo.certifymyrepo.rest.dao;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;
import fr.sedoo.certifymyrepo.rest.service.v1_0.OrcidService;
import fr.sedoo.certifymyrepo.rest.utils.JerseyClient;

@Component
public class OrcidDaoImpl implements OrcidDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrcidService.class);
	
	JerseyClient client;
	
	@Autowired
	public OrcidDaoImpl(JerseyClient client) {
		this.client = client;
	}

	@Override
	public ProfileDto getUserInfoByOrcid(String orcid) {
		String response = client.getJsonResponse(String.format("https://pub.orcid.org/v3.0/%s", orcid));
		if(response != null) {
			return parseResponse(response, orcid);
		} else {
			return null;
		}
	}
	
	private ProfileDto parseResponse(String content, String orcid) {
		ProfileDto user = null;
		JSONParser parser = new JSONParser();
		try {
			JSONObject aux = (JSONObject) parser.parse(content);
			JSONObject person = (JSONObject) aux.get("person");
			JSONObject name = (JSONObject) person.get("name");
			JSONObject givenNames = (JSONObject) name.get("given-names");
			String givenNameValue = givenNames.get("value").toString();
			JSONObject familyName = (JSONObject) name.get("family-name");
			String familyNameValue = familyName.get("value").toString();
			user = new ProfileDto();
			user.setName(givenNameValue.concat(" ").concat(familyNameValue));
			JSONObject emails = (JSONObject) person.get("emails");
			JSONArray emailList = (JSONArray) emails.get("email");
			if(emailList != null && emailList.size() > 0) {
				for(int i=0 ; i<emailList.size() ; i++) {
					JSONObject email = (JSONObject) emailList.get(i);
					boolean isPrimary = Boolean.parseBoolean(email.get("primary").toString());
					if(isPrimary) {
						user.setEmail(email.get("email").toString());
						break;
					}
				}
			}
		} catch (ParseException e) {
			LOG.error(String.format("Error cannot get information from the orcid %s", orcid), e);
		}
		return user;
	}

}
