package fr.sedoo.certifymyrepo.rest.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.dao.CertificationReportTemplateDao;
import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.template.CertificationTemplate;
import fr.sedoo.certifymyrepo.rest.domain.template.RequirementTemplate;
import fr.sedoo.certifymyrepo.rest.dto.CertificationItemDto;
import fr.sedoo.certifymyrepo.rest.dto.ReportDto;
import fr.sedoo.certifymyrepo.rest.dto.RepositoryHealth;

@Component
public class RepositoryHealthCheck {
	
	@Autowired
	CertificationReportTemplateDao templateDao;
	
	
	/**
	 * 
	 * @param report
	 * @param numberOfLevel 
	 * @return health check indicator, label and data list for radar chart
	 */
	public RepositoryHealth compute(CertificationReport report) {
		if(report != null ) {
			
			RepositoryHealth result = new RepositoryHealth();
			// init counters
			Map<String, Integer> avg = new HashMap<String, Integer>();
			
			CertificationTemplate template = templateDao.getCertificationReportTemplate(report.getTemplateId());
			ReportDto latestReport = new ReportDto(report, template);
			result.setLatestReport(latestReport);
			
			List<CertificationItemDto> itemList = new ArrayList<CertificationItemDto>();
			for(CertificationItem item : report.getItems()) {
				CertificationItemDto itemDto = new CertificationItemDto(item);
				for(RequirementTemplate requirementTemplate : template.getRequirements()) {
					if(StringUtils.equals(item.getCode(), requirementTemplate.getCode())) {
						itemDto.setLevelActive(requirementTemplate.isLevelActive());
					}
				}
				itemList.add(itemDto);
				if(itemDto.isLevelActive()) {
					String level = (item.getLevel() != null && item.getLevel() != null)
							? item.getLevel() : null;
					incrementCounter(avg, level);
				}

			}
			latestReport.setItems(itemList);
			
			// Green: all the requirements are at the level 4, 3 or 0 for not applicable.
			// Orange: at least half of the requirements has got a level 4, 3 or 0 for not applicable.
			// Red: more than half of the requirements has got a level lower than 3 or has not been filled yet.
			if((!avg.containsKey("null") || avg.get("null") == 0) 
					&& (!avg.containsKey("1") || avg.get("1") == 0 )
					&& (!avg.containsKey("2") || avg.get("2") == 0)) {
				result.setGreen(Boolean.TRUE);
			} else {
				int occurencies = 0;
				if(avg.containsKey("4")) {
					occurencies += avg.get("4");
				}
				if(avg.containsKey("3")) {
					occurencies += avg.get("3");
				}
				if(avg.containsKey("0")) {
					occurencies += avg.get("0");
				}
				if(occurencies >= report.getItems().size() / 2) {
					result.setOrange(Boolean.TRUE);
				} else {
					result.setRed(Boolean.TRUE);
				}
			}

			return result;
		} else {
			return null;
		}
	}

	/**
	 * Key: Compliance level
	 * Value : number of requirement at this compliance level
	 * @param map
	 * @param key level
	 */
	private void incrementCounter(Map<String, Integer> map, String key) {
		String localKey = key;
		if(key == null) {
			localKey = "null";
		}
		map.putIfAbsent(localKey, 0);
		map.put(localKey, map.get(localKey)+1);
	}

}
