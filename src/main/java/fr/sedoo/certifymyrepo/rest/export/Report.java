package fr.sedoo.certifymyrepo.rest.export;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.sedoo.certifymyrepo.rest.dto.AffiliationDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Report {
	
	private String title;
	private String version;
	private String status;
	private Date updateDate;
	private List<Requirement> requirements;
	@JsonIgnore
	private AffiliationDto affiliation;

}
