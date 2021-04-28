package fr.sedoo.certifymyrepo.rest.filter.jwt;

public class ShibbolethToken {

	private String givenname;
	private String name;
	private String mail;
	
	public ShibbolethToken() {
		super();
	}
	

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}


	public String getGivenname() {
		return givenname;
	}


	public void setGivenname(String givenname) {
		this.givenname = givenname;
	}
		
	
}
