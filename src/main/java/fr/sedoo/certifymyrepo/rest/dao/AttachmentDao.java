package fr.sedoo.certifymyrepo.rest.dao;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Async;

import fr.sedoo.certifymyrepo.rest.ftp.DomainFilter;

public interface AttachmentDao {
	
	/**
	 * @param folderName id of the report
	 * @return true is the given folder name exist on root location
	 */
	boolean deleteFile(String folderName, String fileName);
	
	/**
	 * @param folderName id of the repository
	 * @return true is the given folder name exist on root location
	 */
	boolean deleteAllFilesInFolder(String folderName);
	
	/**
	 * List all files on crusöe FTP
	 * report-uuid
	 * 	|_ requirement code 0
	 * 	|		|_ files
	 *  |_ requirement code 1
	 * 	|		|_ files
	 *  ..
	 *  |_ requirement code n
	 * 			|_ files
	 * @param folderName the first folder name match with report uuid
	 * @return map [codeRequirement, files list], [codeRequirement, files list], etc
	 */
	Map<String, List<String>> listFiles(String folderName);
	
	/**
	 * Download the 
	 * @param fileName  fileName
	 * @param localFolder destination file on local machine
	 * @param ftpPath path of the file on the FTP server
	 */
	void downloadFile(String fileName, File localFolder, String ftpPath);
	
	/**
	 * Download files from a folder and its sub folder
	 * @param localFolder folder on local machine where files will be downloaded
	 * @param folderName root folder name on FTP server
	 * @param domainFilter optional filter (not implemented yet)
	 */
	boolean downloadFiles(File localFolder, String folderName, DomainFilter domainFilter);
	
	/**
	 * Upload file on FTP server
	 * @param inputStream file input stream to upload
	 * @param path folder separated by / (Example: toto/tata) will be created if needed
	 * @param fileName name of the file on the FTP server (can be different than original file name)
	 */
	void uploadFile(InputStream inputStream, String path, String fileName);
	
	/**
	 * Upload files on FTP server
	 * @param localFolder destination file on local machine
	 * @param folderName root folder name of the file on the FTP server
	 */
	void uploadFiles(File localFolder, String folderName);
	
	/**
	 * Copy files from on folder to another on the FTP server
	 * @param localFolder destination file on local machine
	 * @param originalFolderName original root folder
	 * @param destinationFolderName destination root folder
	 */
	@Async
	void copyFiles(File localFolder, String originalFolderName, String destinationFolderName);

}
