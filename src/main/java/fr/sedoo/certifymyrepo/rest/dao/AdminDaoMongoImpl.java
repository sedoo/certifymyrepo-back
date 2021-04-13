package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Admin;

@Component
public class AdminDaoMongoImpl implements AdminDao {
	
	@Autowired
	private AdminRepository adminRepository;

	@Override
	public boolean isAdmin(String userId) {
		Admin result = adminRepository.findByUserId(userId);
		if(result != null && !result.isSuperAdmin()) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isSuperAdmin(String userId) {
		Admin result = adminRepository.findByUserId(userId);
		if(result != null && result.isSuperAdmin()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Admin save(Admin entity) {
		return adminRepository.save(entity);
	}

	@Override
	public List<Admin> findAll() {
		return adminRepository.findAll();
	}

	@Override
	public Admin findByUserId(String userId) {
		return adminRepository.findByUserId(userId);
	}

	@Override
	public List<Admin> findAllSuperAdmin() {
		return adminRepository.findAllSuperAdmin();
	}
	
	@Override
	public void delete(String userId) {
		adminRepository.deleteById(userId);
	}

	@Override
	public void deleteByUserId(String userId) {
		adminRepository.deleteByUserId(userId);
	}

}
