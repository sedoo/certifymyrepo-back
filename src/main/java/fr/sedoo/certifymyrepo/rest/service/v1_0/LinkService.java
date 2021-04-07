package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.domain.SimpleFtpClient;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.utils.MimeTypeUtils;

@RestController
@CrossOrigin
@RequestMapping(value = "/link")
public class LinkService {
	
	private static final Logger LOG = LoggerFactory.getLogger(LinkService.class);
	
	@Autowired
	private ApplicationConfig config;
	
	@Autowired
	SimpleFtpClient ftpClient;
	
	@RequestMapping(path = "/{reportId}/{codeRequirement}/{fileName}", method = RequestMethod.GET)
	public void link(
			HttpServletResponse response,
			@PathVariable ("reportId") String reportId,         
			@PathVariable ("codeRequirement") String codeRequirement,
			@PathVariable ("fileName") String fileName) { 

		
		String temporaryFolderName = config.getTemporaryDownloadFolderName();
		File workDirectory = new File(temporaryFolderName);
		if (workDirectory.exists() == false) {
			workDirectory.mkdirs();
		}
		File requestFolder = new File(workDirectory, UUID.randomUUID().toString());
		requestFolder.mkdirs();
		
		String localFileAbsoluteName = requestFolder.getAbsolutePath().concat("/").concat(fileName);
		ftpClient.downloadFile(fileName, localFileAbsoluteName, reportId.concat("/").concat(codeRequirement));

        response.setContentType(MimeTypeUtils.getMimeType(fileName));
        
        File file = new File(localFileAbsoluteName);
        byte[] content;
        ByteArrayInputStream bais = null;
		try {
			content = FileUtils.readFileToByteArray(file);
			bais = new ByteArrayInputStream(content);
	        IOUtils.copy(bais, response.getOutputStream());
		} catch (IOException e) {
			LOG.error("Error while downloading file",e);
		} finally {
			if(bais != null) {
				try {
					bais.close();
				} catch (IOException e) {
					LOG.error("Error while closing stream",e);
				}
			}
		}


		

	}

}
