package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.Admin;

public interface AdminDao {
	
	public boolean isAdmin(String userId);
	public boolean isSuperAdmin(String userId);
	public void delete(String adminId);
	public void deleteByUserId(String userId);
	public Admin save(Admin admin);
	public List<Admin> findAll();
	public List<Admin> findAllSuperAdmin();
	public List<Admin> findAllFunctaionalAdmin();
	public Admin findByUserId(String userId);

}
