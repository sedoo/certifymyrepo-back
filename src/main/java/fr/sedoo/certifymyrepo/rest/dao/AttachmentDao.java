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
	 * List all files
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
	 * @param filePath path of the file on the server
	 */
	void downloadFile(String fileName, File localFolder, String filePath);
	
	/**
	 * Download files from a folder and its sub folder
	 * @param localFolder folder on local machine where files will be downloaded
	 * @param folderName root folder name on server
	 * @param domainFilter optional filter (not implemented yet)
	 */
	boolean downloadFiles(File localFolder, String folderName, DomainFilter domainFilter);
	
	/**
	 * Save file on server
	 * @param inputStream file input stream to upload
	 * @param path folder separated by / (Example: toto/tata) will be created if needed
	 * @param fileName name of the file on the server (can be different than original file name)
	 */
	void saveFile(InputStream inputStream, String path, String fileName);
	
	/**
	 * Copy File source to target
	 * @param sourceFolder
	 * @param targetFolder
	 */
	void copyDirectory(File sourceFolder, File targetFolder);

}
