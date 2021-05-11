package fr.sedoo.certifymyrepo.rest.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = Affiliation.COLLECTION_NAME, language = "english")
public class Affiliation {

	public final static String COLLECTION_NAME = "affiliations";

	@Id
	private String id;
	private String institute;
	private String acronym;
	private String department;
	private String address;
	private String country;
	private String website;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Affiliation other = (Affiliation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
