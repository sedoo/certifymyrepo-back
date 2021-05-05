package fr.sedoo.certifymyrepo.rest.dto;

import fr.sedoo.certifymyrepo.rest.domain.CertificationItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificationItemDto {
	
	/**
	 * Set Report collection information into DTO
	 * Leave information from template to null
	 * @param item CertificationItem from report mongoDB collection
	 */
	public CertificationItemDto(CertificationItem item) {
		this.setCode(item.getCode());
		this.setLevelCode(item.getLevel());
		this.setResponse(item.getResponse());
	}
	
	private String code;
	private String response;
	private String levelCode;
	private String levelLabel;
	private boolean levelActive;

}
