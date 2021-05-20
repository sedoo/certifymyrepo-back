package fr.sedoo.certifymyrepo.rest.ftp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainFilter {

	/**
	 * Used to select a file
	 * @param fileName
	 * @return true if the file has to be downloaded
	 */
	public boolean isFiltered(String fileName) {
		return true;
	}

}
