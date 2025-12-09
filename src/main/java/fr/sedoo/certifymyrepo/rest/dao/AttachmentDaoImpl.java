package fr.sedoo.certifymyrepo.rest.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.ApplicationConfig;
import fr.sedoo.certifymyrepo.rest.ftp.DomainFilter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AttachmentDaoImpl implements AttachmentDao {

	@Autowired
	private ApplicationConfig config;

	public boolean deleteFile(String folderName, String fileName) {
	    try {
	        Path root = Paths.get(config.getRootDir());
	        Path filePath = (folderName == null || folderName.isEmpty())
	                ? root.resolve(fileName)
	                : root.resolve(folderName).resolve(fileName);

	        return Files.deleteIfExists(filePath);
	    } catch (IOException e) {
	        return false;
	    }
	}

	@Override
	public boolean deleteAllFilesInFolder(String folderName) {
	    try {
	        Path folder = Paths.get(config.getRootDir(), folderName);

	        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
	            return false;
	        }

	        try (var files = Files.list(folder)) {
	            files.forEach(path -> {
	                try {
	                    Files.deleteIfExists(path);
	                } catch (IOException ignored) {
	                }
	            });
	        }

	        return true;
	    } catch (IOException e) {
	        return false;
	    }
	}

	@Override
	public Map<String, List<String>> listFiles(String folderName) {
	    Map<String, List<String>> map = new HashMap<>();

	    if (folderName == null || folderName.trim().isEmpty()) {
	        return map;
	    }

	    try {
	        Path root = Paths.get(config.getRootDir());
	        Path folder = root.resolve(folderName).normalize();

	        // On vérifie que le dossier existe
	        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
	            return map;
	        }

	        // Liste les sous-dossiers
	        try (var stream = Files.list(folder)) {
	            for (Path subPath : stream.toList()) {

	                if (Files.isDirectory(subPath)) {
	                    String subFolderName = subPath.getFileName().toString();

	                    // Liste les fichiers dans ce sous-dossier
	                    try (var fileStream = Files.list(subPath)) {

	                        List<String> fileNames = fileStream
	                                .filter(Files::isRegularFile)
	                                .map(p -> p.getFileName().toString())
	                                .sorted(String.CASE_INSENSITIVE_ORDER)
	                                .toList();

	                        map.put(subFolderName, fileNames);
	                    }
	                }
	            }
	        }

	    } catch (IOException e) {
	        log.error("Error while listing files locally", e);
	    }

	    return map;
	}

	@Override
	public void uploadFile(InputStream inputStream, String path, String fileName) {
	    if (inputStream == null || fileName == null || fileName.isEmpty()) {
	        throw new IllegalArgumentException("Invalid input stream or file name");
	    }

	    try {
	        Path root = Paths.get(config.getRootDir()).toAbsolutePath().normalize();
	        Path targetDir = (path == null || path.isEmpty())
	                ? root
	                : root.resolve(path).normalize();

	        // Vérifier que le chemin reste dans rootDir (sécurité)
	        if (!targetDir.startsWith(root)) {
	            throw new SecurityException("Invalid target path");
	        }

	        // Créer le dossier si nécessaire
	        Files.createDirectories(targetDir);

	        // Construire le chemin complet du fichier
	        Path filePath = targetDir.resolve(fileName).normalize();

	        // Écrire le fichier (écrase si déjà présent)
	        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

	        log.info("File uploaded successfully: {}", filePath);

	    } catch (IOException e) {
	        log.error("Error while uploading file", e);
	        throw new RuntimeException("Upload failed", e);
	    }
	}
	
	@Override
	public void copyFiles(File localFolder, String originalFolderName, String destinationFolderName) {
	    boolean isFiles = this.downloadFiles(localFolder, originalFolderName, new DomainFilter());
	    if (isFiles) {
	        this.uploadFiles(localFolder, destinationFolderName);
	    }
	}

	private void uploadFiles(File localFolder, String rootFolderName) {

	    if (localFolder == null || !localFolder.isDirectory()) {
	        throw new IllegalArgumentException("localFolder must be a directory");
	    }

	    try {
	        Path root = Paths.get(config.getRootDir()).toAbsolutePath().normalize();

	        // Dossier racine où tout sera uploadé
	        Path destinationRoot = (rootFolderName == null || rootFolderName.isEmpty())
	                ? root
	                : root.resolve(rootFolderName).normalize();

	        // Sécurité
	        if (!destinationRoot.startsWith(root)) {
	            throw new SecurityException("Invalid root folder");
	        }

	        // Crée le dossier racine s'il n'existe pas
	        Files.createDirectories(destinationRoot);

	        // Collecte les fichiers comme dans la version FTP
	        List<File> attachments = new ArrayList<>();

	        File[] allRequirementFolders = localFolder.listFiles();
	        if (allRequirementFolders != null) {
	            for (File requirementFolder : allRequirementFolders) {
	                if (requirementFolder.isDirectory()) {
	                    File[] requirementFiles = requirementFolder.listFiles();
	                    if (requirementFiles != null) {
	                        for (File requirementFile : requirementFiles) {
	                            if (requirementFile.isFile()) {
	                                attachments.add(requirementFile);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        // Upload des fichiers
	        for (File attachment : attachments) {
	            try (InputStream fis = new FileInputStream(attachment)) {

	                // Equivalent du code FTP : récupérer le nom du sous-dossier
	                String parentPath = attachment.getParent();
	                String folder = parentPath.substring(parentPath.lastIndexOf(File.separator));

	                String fullTargetFolder = rootFolderName + folder;

	                uploadFile(fis, fullTargetFolder, attachment.getName());

	            } catch (IOException e) {
	                log.error("Error while uploading file {}", attachment.getName(), e);
	                throw e;
	            }
	        }

	    } catch (IOException e) {
	        log.error("Error while handling upload to {}", rootFolderName, e);
	    }
	}


	@Override
	public boolean downloadFiles(File localFolder, String folderName, DomainFilter domainFilter) {
	        return false;
	}


	@Override
	public void downloadFile(String fileName, File localFolder, String ftpPath) {

	}

	@Override
	public void copyDirectory(File sourceFolder, File targetFolder) {
	    try {
		    Path sourcePath = sourceFolder.toPath();
		    Path targetPath = targetFolder.toPath();
			Files.createDirectories(targetPath);
			
		    Files.walk(sourcePath).forEach(source -> {
		        try {
		            Path destination = targetPath.resolve(sourcePath.relativize(source));

		            if (Files.isDirectory(source)) {
		                Files.createDirectories(destination);
		            } else {
		                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
		            }
		        } catch (IOException e) {
		            throw new UncheckedIOException(e);
		        }
		    });
		} catch (IOException e) {
			log.error("Cannot copy files", e);
		}
	}


}
