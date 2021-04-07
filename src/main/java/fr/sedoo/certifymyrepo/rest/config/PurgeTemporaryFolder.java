package fr.sedoo.certifymyrepo.rest.config;

import java.io.File;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

@Component
public class PurgeTemporaryFolder {

	@Autowired
	ApplicationConfig config;

	@Scheduled(cron = "${temporary.cronExpression}")
	public void purgeTemporaryFolder() {

		File temporaryFolder = new File(config.getTemporaryDownloadFolderName());
		if (!temporaryFolder.exists()) {
			return;
		} else {
			File[] listFiles = temporaryFolder.listFiles();
			Date currentDate = new Date();
			for (int i = 0; i < listFiles.length; i++) {
				File current = listFiles[i];
				current.lastModified();
				Date lastModified = new Date(current.lastModified());
				int aux = Math.abs(Days.daysBetween(new DateTime(lastModified), new DateTime(currentDate)).getDays());
				if (aux > config.getStorageDuration()) {
					if (current.isDirectory()) {
						FileSystemUtils.deleteRecursively(current);
					} else {
						current.delete();
					}
				}
			}
		}

	}

}
