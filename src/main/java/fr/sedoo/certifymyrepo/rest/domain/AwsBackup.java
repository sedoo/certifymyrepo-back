package fr.sedoo.certifymyrepo.rest.domain;

import org.springframework.scheduling.annotation.Scheduled;

public class AwsBackup {

	
	@Scheduled(cron="0 0 1 * * ?")
	public void dailyBackup() {
	
	}

}
