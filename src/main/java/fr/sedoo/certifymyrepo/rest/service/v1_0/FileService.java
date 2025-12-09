package fr.sedoo.certifymyrepo.rest.service.v1_0;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.dao.AttachmentDao;
import fr.sedoo.certifymyrepo.rest.utils.MimeTypeUtils;

@RestController
@CrossOrigin
@RequestMapping(value = "/file/v1_0")
public class FileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
	
	@Autowired
	AttachmentDao ftpClient;
	
	@Autowired
	private ApplicationConfig config;
	
	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(path = "/{reportId}/{codeRequirement}/{fileName}", method = RequestMethod.GET)
	public void download(
	        HttpServletRequest request,
	        HttpServletResponse response,
	        @PathVariable("reportId") String reportId,
	        @PathVariable("codeRequirement") String codeRequirement,
	        @PathVariable("fileName") String fileName) {

	    try {
	        // Construction sécurisée du chemin
	        Path root = Paths.get(config.getRootDir()).toAbsolutePath().normalize();
	        Path filePath = root
	                .resolve(reportId)
	                .resolve(codeRequirement)
	                .resolve(fileName)
	                .normalize();

	        // Protection contre les chemins malicieux
	        if (!filePath.startsWith(root)) {
	            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
	            return;
	        }

	        // Vérification existence
	        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
	            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
	            return;
	        }

	        // Type MIME
	        response.setContentType(MimeTypeUtils.getMimeType(fileName));

	        // Taille du fichier
	        response.setContentLengthLong(Files.size(filePath));

	        // Force le téléchargement (évite certains comportements navigateurs)
	        response.setHeader(
	                "Content-Disposition",
	                "attachment; filename=\"" + fileName + "\""
	        );

	        // Stream du fichier
	        try (InputStream in = Files.newInputStream(filePath);
	             OutputStream out = response.getOutputStream()) {
	            IOUtils.copyLarge(in, out);
	            out.flush();
	        }

	    } catch (IOException e) {
	        LOG.error("Error while downloading file {}", fileName, e);
	        try {
	            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to download file");
	        } catch (IOException ex) {
	            LOG.error("Unable to send error response", ex);
	        }
	    }
	}


	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(path = "/upload", method = RequestMethod.POST)
	public List<String> uploadFile(
			HttpServletRequest request,
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
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
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

	@PreAuthorize("@permissionEvaluator.isUser(#request)")
	@RequestMapping(path = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<String> delete(
			HttpServletRequest request,
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
