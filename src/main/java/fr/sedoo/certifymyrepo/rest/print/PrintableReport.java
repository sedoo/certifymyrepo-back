package fr.sedoo.certifymyrepo.rest.print;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintableReport {
	
	private String title;
	private String version;
	private String status;
	private Date updateDate;
	private List<PrintableRequirement> requirements;

}
