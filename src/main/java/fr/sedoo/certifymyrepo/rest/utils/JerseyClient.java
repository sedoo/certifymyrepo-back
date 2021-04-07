package fr.sedoo.certifymyrepo.rest.utils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import fr.sedoo.certifymyrepo.rest.config.utils.SslUtils;

@Component
public class JerseyClient {
	
	public String getJsonResponse(String url) {
		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new HTTPSProperties(SslUtils.getHostnameVerifier(), SslUtils.getSslContext()));
		Client client = Client.create(config);
		WebResource webResource = client.resource(url);
		ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
		int status = response.getStatus();
		if (status == HttpStatus.OK.value()) {
			return response.getEntity(String.class);
		} else {
			return null;
		}
	}

}
