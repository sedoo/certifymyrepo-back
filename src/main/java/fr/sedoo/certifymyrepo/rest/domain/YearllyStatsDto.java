package fr.sedoo.certifymyrepo.rest.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class YearllyStatsDto {
	
	private long year;
	private Stats firstSemester;
	private Stats secondSemester;

}
