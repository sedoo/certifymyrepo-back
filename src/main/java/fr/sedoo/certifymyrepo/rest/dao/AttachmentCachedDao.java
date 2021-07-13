package fr.sedoo.certifymyrepo.rest.dao;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.sedoo.certifymyrepo.rest.ftp.DomainFilter;

/**
 * Map<String, List<String>> listFiles(String folderName) scan the FTP server and return all files name for a report uuid
 * This class add a cache for this service
 */
public class AttachmentCachedDao implements AttachmentDao {
	
	private AttachmentDao proxyDao;

	public AttachmentCachedDao(AttachmentDao proxyDao) {
		super();
		this.proxyDao = proxyDao;
	}
	
	LoadingCache<String, Map<String, List<String>>> cache = CacheBuilder.newBuilder().maximumSize(100)
			.expireAfterWrite(10, TimeUnit.MINUTES).build(new CacheLoader<String, Map<String, List<String>>>() {
				@Override
				public Map<String, List<String>> load(String folderName) {
					return proxyDao.listFiles(folderName);
				}
			});

	@Override
	public boolean deleteFile(String ftpPath, String fileName) {
		cache.invalidate(ftpPath.split("/")[0]);
		return proxyDao.deleteFile(ftpPath, fileName);
	}

	@Override
	public boolean deleteAllFilesInFolder(String folderName) {
		cache.invalidate(folderName);
		return proxyDao.deleteAllFilesInFolder(folderName);
	}

	@Override
	public Map<String, List<String>> listFiles(String folderName) {
		try {
			return cache.get(folderName);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void downloadFile(String fileName, File localFolder, String ftpPath) {
		proxyDao.downloadFile(fileName, localFolder, ftpPath);
	}

	@Override
	public boolean downloadFiles(File localFolder, String folderName, DomainFilter domainFilter) {
		return proxyDao.downloadFiles(localFolder, folderName, domainFilter);
	}

	@Override
	public void uploadFile(InputStream inputStream, String ftpPath, String fileName) {
		cache.invalidate(ftpPath.split("/")[0]);
		proxyDao.uploadFile(inputStream, ftpPath, fileName);

	}

	@Override
	public void uploadFiles(File localFolder, String folderName) {
		cache.invalidate(folderName);
		proxyDao.uploadFiles(localFolder, folderName);
	}

	@Override
	public void copyFiles(File localFolder, String originalFolderName, String destinationFolderName) {
		proxyDao.copyFiles(localFolder, originalFolderName, destinationFolderName);
	}

}
