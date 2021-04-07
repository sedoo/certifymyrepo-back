package fr.sedoo.certifymyrepo.rest.domain;

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
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.config.FtpConfiguration;
import fr.sedoo.certifymyrepo.rest.service.exception.DownloadException;
import fr.sedoo.certifymyrepo.rest.service.exception.BusinessException;

@Component
public class SimpleFtpClient {

	Logger logger = LoggerFactory.getLogger(SimpleFtpClient.class);

	private FtpConfiguration ftpConfiguration;

	@Autowired
	public SimpleFtpClient(FtpConfiguration ftpConfiguration) {
		this.ftpConfiguration = ftpConfiguration;
	}
	
	/**
	 * @param folderName id of the report
	 * @return true is the given folder name exist on root location
	 */
	public boolean checkDirectoryExistance(String folderName) {
		boolean result = false;
		FTPClient client = new FTPClient();
		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (folderName != null) {
				FTPFile[] dirs = client.listDirectories();
				if(dirs != null && dirs.length > 0) {
					for(FTPFile dir : dirs) {
						if(StringUtils.equals(folderName, dir.getName())) {
							result = true;
							break;
						}
					}
				}
			}
			client.disconnect();
		} catch (SocketException e) {
			logger.error("Error checking folder", e);
		} catch (IOException e) {
			logger.error("Errorchecking folder", e);
		}
		return result;
	}
	
	/**
	 * @param folderName id of the report
	 * @return true is the given folder name exist on root location
	 */
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
					throw new RuntimeException("The corresponding folder doesn't exist");
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
	public Map<String, List<String>> listFiles(String folderName) {
		
		Map<String, List<String>> map = new HashMap<>();
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
					throw new BusinessException("The corresponding folder doesn't exist");
				}
			}
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

	/**
	 * @param source
	 *            fileName
	 * @param destination
	 *            full file path of on local machine
	 * @throws IOException
	 *             {@link IOException}
	 */
	public void downloadFile(String source, String destination, String folderName) {
		FTPClient client = new FTPClient();
		FileOutputStream out = null;
		try {
			client.connect(ftpConfiguration.getHost());
			client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			client.enterLocalPassiveMode();
			client.login(ftpConfiguration.getLogin(), ftpConfiguration.getPassword());
			if (folderName != null) {
				client.changeWorkingDirectory(folderName);
			}
			if (isFileExist(client, source)) {
				client.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
				client.setFileTransferMode(FTP.BINARY_FILE_TYPE);
				out = new FileOutputStream(destination);
				client.retrieveFile(source, out);
				out.close();
			}
			client.disconnect();
		} catch (SocketException e) {
			logger.error("Socket issue durng downloading", e);
			throw new DownloadException("Socket issue durng downloading");
		} catch (IOException e) {
			logger.error("I/O issue durng downloading", e);
			throw new DownloadException("I/O issue durng downloading");
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

	/**
	 * 
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
	
	/**
	 * Upload file on FTP server
	 * @param inputStream file input stream to upload
	 * @param path folder separated by / (Example: toto/tata) will be created if needed
	 * @param fileName name of the file on the FTP server (can be different than original file name)
	 */
	public void uploadFile(InputStream inputStream, String path, String fileName) {
		FTPClient client = new FTPClient();
		FileInputStream fis = null;
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
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.error("FileInputStream counld not be closed");
				}
			}
		}
	}

}
