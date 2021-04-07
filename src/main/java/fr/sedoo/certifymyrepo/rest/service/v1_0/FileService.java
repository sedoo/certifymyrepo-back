package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.sedoo.certifymyrepo.rest.domain.SimpleFtpClient;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;

@RestController
@CrossOrigin
@RequestMapping(value = "/file/v1_0")
public class FileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
	
	@Autowired
	SimpleFtpClient ftpClient;

	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(path = "/upload", method = RequestMethod.POST)
	public List<String> uploadFile(
			@RequestHeader("Authorization") String authHeader,
	        @RequestPart("files") MultipartFile[] files, 
	        @RequestParam("reportId") String reportId,         
	        @RequestParam("codeRequirement") String codeRequirement) { 
		InputStream is = null;
		List<String> filesUploaded = new ArrayList<>();
		try {
			if(files != null && files.length > 0) {
				for(MultipartFile file : files) {
					is = file.getInputStream();
					String formattedName = formatFileName(file.getOriginalFilename());
					ftpClient.uploadFile(is, reportId.concat("/").concat(codeRequirement), formattedName);
					is.close();
					filesUploaded.add(formattedName);
				}
			}
		} catch (IOException e) {
			LOG.error("Error while uploading file for report id {} and requirement {}", e, reportId, codeRequirement);
			throw new RuntimeException("Error while uploading files");
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.error("Error while closing stream", e);
				}
			}
		}
	    return filesUploaded;
	}
	
	/**
	 * Format file name
	 * @param originalName
	 * @return name without accent or space
	 */
	private String formatFileName(String originalName) {
		String formatedName = unaccent(originalName);
		formatedName = formatedName.replace(" ", "_");
		return formatedName;
	}
	
	/**
	 * Removes any accentuation from the string by replacing single characters without an accent.
	 * @param src
	 * @return string with accent
	 */
	private String unaccent(String src) {
		return Normalizer
				.normalize(src, Normalizer.Form.NFD)
				.replaceAll("[^\\p{ASCII}]", "");
	}

	@Secured({Roles.AUTHORITY_USER})
	@RequestMapping(path = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<String> delete(
			@RequestHeader("Authorization") String authHeader,
	        @RequestParam("reportId") String reportId,         
	        @RequestParam("codeRequirement") String codeRequirement,
	        @RequestParam("fileName") String fileName) { 
		ResponseEntity<String> result = null;
		if(ftpClient.deleteFile(reportId.concat("/").concat(codeRequirement), fileName)) {
			result = new ResponseEntity<String>(HttpStatus.OK);
		} else {
			result = new ResponseEntity<String>(fileName.concat(" file could not be deleted"),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return result;
	}
}
