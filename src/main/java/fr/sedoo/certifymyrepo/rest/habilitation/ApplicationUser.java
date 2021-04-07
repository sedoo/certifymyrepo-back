package fr.sedoo.certifymyrepo.rest.habilitation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class ApplicationUser extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5627123822155839204L;
	private String name;
	private String uuid;
	private Set<String> roles;

	public ApplicationUser(String uuid) {
		super(uuid, "not_applicable", new ArrayList<>());
		this.uuid = uuid;
	}

	public ApplicationUser(String uuid, String name, Collection<? extends GrantedAuthority> authorities) {
		super(uuid, "not_applicable", authorities);
		this.uuid = uuid;
		this.name = name;
		this.roles = new HashSet<String>();
		for (GrantedAuthority a : authorities) {
			roles.add(a.getAuthority());
		}
	}

	public String getUserId() {
		return getUsername();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addRole(String role) {
		roles.add(role);
		getAuthorities().add(new SimpleGrantedAuthority(role));
	}

	public Set<String> getRoles() {
		return roles;
	}

	public boolean isAdmin() {
		return hasRole(Roles.AUTHORITY_ADMIN);
	}
	
	public boolean isSuperAdmin() {
		return hasRole(Roles.AUTHORITY_SUPER_ADMIN);
	}

	public boolean hasRole(String role) {
		return getAuthorities().contains(new SimpleGrantedAuthority(role));
	}

}
