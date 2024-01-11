package fr.sedoo.certifymyrepo.rest.dao;

import java.util.Collections;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import fr.sedoo.certifymyrepo.rest.dto.ProfileDto;
import fr.sedoo.certifymyrepo.rest.service.v1_0.OrcidService;
import lombok.Setter;

@Component
@Setter
public class OrcidDaoImpl implements OrcidDao {
	
	private static final Logger LOG = LoggerFactory.getLogger(OrcidService.class);
	
	private RestTemplate restTemplate = new RestTemplate();

	@Override
	public ProfileDto getUserInfoByOrcid(String orcid) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
        		String.format("https://pub.orcid.org/v3.0/%s", orcid),
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String response = responseEntity.getBody();
            if (response != null) {
                return parseResponse(response, orcid);
            }	
        }
        
        return null;
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
