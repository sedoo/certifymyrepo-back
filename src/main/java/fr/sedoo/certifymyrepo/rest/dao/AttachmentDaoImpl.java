package fr.sedoo.certifymyrepo.rest.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import fr.sedoo.certifymyrepo.rest.config.FtpConfiguration;
import fr.sedoo.certifymyrepo.rest.ftp.DomainFilter;
import fr.sedoo.certifymyrepo.rest.service.exception.DownloadException;

@Component
public class AttachmentDaoImpl implements AttachmentDao {
	
	Logger logger = LoggerFactory.getLogger(AttachmentDaoImpl.class);

	private FtpConfiguration ftpConfiguration;

	@Autowired
	public AttachmentDaoImpl(FtpConfiguration ftpConfiguration) {
		this.ftpConfiguration = ftpConfiguration;
	}

	@Override
	public boolean deleteFile(String folderName, String fileName) {
		boolean resultDelete = false;
		FTPClient client = new FTPClient();
		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (folderName != null) {
				boolean result = client.changeWorkingDirectory(folderName);
				if (result == false) {
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "The corresponding folder doesn't exist");
				}
			}
			resultDelete = client.deleteFile(fileName);
			client.disconnect();
		} catch (SocketException e) {
			logger.error("Error checking folder", e);
		} catch (IOException e) {
			logger.error("Errorchecking folder", e);
		}
		return resultDelete;
	}

	@Override
	public boolean deleteAllFilesInFolder(String folderName) {
		boolean resultDelete = false;
		FTPClient client = new FTPClient();

		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (folderName != null) {
				boolean result = client.changeWorkingDirectory(folderName);
				if (result == true) {
					FTPFile[] listFiles = client.listFiles();
					for (int i = 0; i < listFiles.length; i++) {
						if(listFiles[i].isDirectory()) {
							String codeRequirement = listFiles[i].getName();
							client.changeWorkingDirectory(codeRequirement);
							FTPFile[] listFilesByRequirement = client.listFiles();
							if(listFilesByRequirement != null) {
								for(FTPFile file : listFilesByRequirement) {
									client.deleteFile(file.getName());
								}
							}
							client.changeToParentDirectory();
							resultDelete = client.removeDirectory(codeRequirement);
							
						}
					}
					client.changeToParentDirectory();
					resultDelete = client.removeDirectory(folderName);
				}
			}
		} catch (IOException e) {
			logger.error("Error while listing files", e);
		} finally {
			try {
				client.disconnect();
			} catch (IOException e) {
				logger.error("Error while disconnecting from FTP", e);
			}
		}
		return resultDelete;
	}

	@Override
	public Map<String, List<String>> listFiles(String folderName) {
		Map<String, List<String>> map = null;
		FTPClient client = new FTPClient();

		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (folderName != null) {
				boolean result = client.changeWorkingDirectory(folderName);
				if (result == true) {
					map = new HashMap<>();
					FTPFile[] listFiles = client.listFiles();
					for (int i = 0; i < listFiles.length; i++) {
						if(listFiles[i].isDirectory()) {
							String codeRequirement = listFiles[i].getName();
							client.changeWorkingDirectory(codeRequirement);
							FTPFile[] listFilesByRequirement = client.listFiles();
							if(listFilesByRequirement != null) {
								List<String> filesName = new ArrayList<>();
								for(FTPFile file : listFilesByRequirement) {
									filesName.add(file.getName());
								}
								Comparator<String> compareByName = (String o1, String o2) -> o1.compareToIgnoreCase(o2);
								filesName.sort(compareByName);
								map.put(codeRequirement, filesName);
							}
							client.changeToParentDirectory();
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("Error while listing files", e);
		} finally {
			try {
				client.disconnect();
			} catch (IOException e) {
				logger.error("Error while disconnecting from FTP", e);
			}
		}
		return map;
	}

	@Override
	public void downloadFile(String fileName, File localFolder, String ftpPath) {
		FTPClient client = new FTPClient();
		FileOutputStream out = null;
		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (ftpPath != null) {
				client.changeWorkingDirectory(ftpPath);
			}
			if (isFileExist(client, fileName)) {
				client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
				client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
				File localFile = new File(localFolder, fileName);
				out = new FileOutputStream(localFile);
				client.retrieveFile(fileName, out);
				out.close();
			}
			client.disconnect();
		} catch (SocketException e) {
			logger.error("Socket issue durng downloading", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} catch (IOException e) {
			logger.error("I/O issue durng downloading", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("Error while close stream", e);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param fileName
	 * @return true if file passed as parameter exist on FTP server
	 * @throws IOException
	 */
	private boolean isFileExist(FTPClient client, String fileName) throws IOException {
		boolean result = false;
		FTPFile[] files = client.listFiles();
		if (files != null && files.length > 0) {
			for (FTPFile file : files) {
				if (StringUtils.equals(file.getName(), fileName)) {
					result = true;
				}
			}
		}
		return result;
	}

	@Override
	public void downloadFiles(File localFolder, String folderName, DomainFilter domainFilter) {
		FTPClient client = new FTPClient();

		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (folderName != null) {
				boolean result = client.changeWorkingDirectory(folderName);
				if (result == false) {
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "The root folder does not exist");
				}
			}
			FTPFile[] listFiles = client.listFiles();

			for (int i = 0; i < listFiles.length; i++) {
				downloadFile(localFolder, listFiles[i], client, domainFilter);
			}

			client.disconnect();
		} catch (IOException e) {
			logger.error("Error while download content {}", folderName, e);
		} catch (DownloadException e) {
			logger.error("Error while download content folder {} does not exist", folderName, e);
		}
	}
	
	private void downloadFile(File localFolder, FTPFile ftpFile, FTPClient client, DomainFilter domainFilter) throws IOException {

		if (ftpFile.isFile()) {
			if (!isDownloadableFile(ftpFile)) {
				return;
			}
			if (domainFilter.isFiltered(ftpFile.getName())) {

				File localFile = new File(localFolder, ftpFile.getName());
				try (FileOutputStream fos = new FileOutputStream(localFile)) {
					client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
					client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
					logger.info("Downloading file: " + ftpFile.getName());
					client.retrieveFile(ftpFile.getName(), fos);
					logger.info("Download completed");

				} catch (IOException e) {
					throw e;
				}

			} else {
				logger.info(ftpFile.getName() + " won't be downloaded because of the filter");
			}

		} else {
			File localSubFolder = new File(localFolder, ftpFile.getName());
			localSubFolder.mkdirs();
			client.changeWorkingDirectory(ftpFile.getName());
			FTPFile[] listFiles = client.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				downloadFile(localSubFolder, listFiles[i], client, domainFilter);
			}
			client.changeToParentDirectory();
		}

	}

	private boolean isDownloadableFile(FTPFile ftpFile) {
		return true;
	}

	@Override
	public void uploadFile(InputStream inputStream, String path, String fileName) {
		FTPClient client = new FTPClient();
		try {
			client.connect(ftpConfiguration.getHost());
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			client.setBufferSize(16 * 1024);
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			if (path != null) {
				if (path.contains("/")) {
					String[] folders = path.split("/");
					for (String folder : folders) {
						if (!isFolderExist(client, folder)) {
							client.makeDirectory(folder);
						}
						client.changeWorkingDirectory(folder);
					}
				} else {
					client.changeWorkingDirectory(path);
				}
			}
			// If present on FTP server delete it.
			if (isFileExist(client, fileName)) {
				client.deleteFile(fileName);
			}
			client.storeFile(fileName, inputStream);
			client.disconnect();
		} catch (Exception e) {
			logger.error("Error while uploading file {}", fileName, e);
		}
	}
	
	/**
	 * @param fileName
	 * @return true if file passed as parameter exist on FTP server
	 * @throws IOException
	 */
	private boolean isFolderExist(FTPClient client, String folderName) throws IOException {
		boolean result = false;
		FTPFile[] dirs = client.listDirectories();
		if (dirs != null && dirs.length > 0) {
			for (FTPFile dir : dirs) {
				if (StringUtils.equals(dir.getName(), folderName)) {
					result = true;
				}
			}
		}
		return result;
	}
	
	@Override
	public void uploadFiles(File localFolder, String rootFolderName) {
		FTPClient client = new FTPClient();

		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (rootFolderName != null) {
				client.makeDirectory(rootFolderName);
				boolean result = client.changeWorkingDirectory(rootFolderName);
				if (result == false) {
					throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED, "The root folder does not exist");
				}
			}
			File[] allRequirementFolder = localFolder.listFiles();
			List<File> attachments = new ArrayList<File>();
			for(File requirementFolder : allRequirementFolder) {
				if(requirementFolder.isDirectory()) {
					File[] requirementFiles = requirementFolder.listFiles();
					for(File requirementFile : requirementFiles) {
						if(requirementFile.isFile()) {
							attachments.add(requirementFile);
						}
					}
				}
			}
	
			for (int i = 0; i < attachments.size(); i++) {
				try (InputStream fis = new FileInputStream(attachments.get(i))) {
					String path = attachments.get(i).getParent();
					String folder = path.substring(path.lastIndexOf("/"), path.length());
					uploadFile(fis, rootFolderName.concat(folder), attachments.get(i).getName());
				} catch (IOException e) {
					throw e;
				}
			}

			client.disconnect();
		} catch (IOException e) {
			logger.error("Error while download content {}", rootFolderName, e);
		} catch (DownloadException e) {
			logger.error("Error while download content folder {} does not exist", rootFolderName, e);
		}
	}

}
