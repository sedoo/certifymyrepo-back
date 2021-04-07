package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Admin;

public interface AdminDao {
	
	public boolean isAdmin(String userId);
	public boolean isSuperAdmin(String userId);
	public void delete(String userId);
	public Admin save(Admin admin);
	public List<Admin> findAll();
	public Admin findByUserId(String userId);

}
