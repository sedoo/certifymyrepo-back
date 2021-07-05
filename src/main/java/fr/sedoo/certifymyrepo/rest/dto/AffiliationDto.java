package fr.sedoo.certifymyrepo.rest.dto;

import fr.sedoo.certifymyrepo.rest.domain.Affiliation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AffiliationDto implements Comparable<AffiliationDto> {

	private String id;
	private String institute;
	private String acronym;
	private String department;
	private String country;
	private String website;
	private String address;
	private boolean isInternational;

	public AffiliationDto(Affiliation source) {
		if(source != null) {
			setInstitute(source.getInstitute());
			setAcronym(source.getAcronym());
			setCountry(source.getCountry());
			setId(source.getId());
			setDepartment(source.getDepartment());
			setWebsite(source.getWebsite());
			setAddress(source.getAddress());
			setInternational(source.isInternational());
		}
	}

	@Override
	public int compareTo(AffiliationDto o) {
		return o.getInstitute().compareTo(getInstitute());
	}

}
