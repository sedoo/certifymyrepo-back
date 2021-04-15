package fr.sedoo.certifymyrepo.rest.dto;

import fr.sedoo.certifymyrepo.rest.domain.Affiliation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AffiliationDto implements Comparable<AffiliationDto> {

	private String id;
	private String institute;
	private String departement;
	private String country;

	public AffiliationDto(Affiliation source) {
		if(source != null) {
			setInstitute(source.getInstitute());
			setCountry(source.getCountry());
			setId(source.getId());
			setDepartement(source.getDepartment());
		}

	}

	@Override
	public int compareTo(AffiliationDto o) {
		return o.getInstitute().compareTo(getInstitute());
	}

}
