package fr.sedoo.certifymyrepo.rest.service.notification;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dao.CertificationReportDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;

@Component
public class ActionReminder {
	
	@Value("${notification.unused.report.months.delay}")
	private Integer delay;
	
	@Autowired
	CertificationReportDao reportDao;

	@Scheduled(cron = "${notification.cronExpression}")
	public void notificationUnsedRepository() {
		
		Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, -delay);
		List<CertificationReport> reportList = reportDao.findInProgressByUpdateDateLowerThan(c.getTime());
		if(reportList != null) {
			for(CertificationReport report : reportList) {
				
			}
		}
	}

}
