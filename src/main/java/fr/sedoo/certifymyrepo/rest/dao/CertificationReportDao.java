package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;

public interface CertificationReportDao {

	CertificationReport save(CertificationReport certificationReport);
	
	void delete(String id);
	
	void deleteByRepositoryId(String repositoryId);

	List<CertificationReport> findByRepositoryId(String repositoryId);
	
	CertificationReport findById(String id);
	
	CertificationReport findByRepositoryIdAndMaxUpdateDate(String repositoryId);
	
	CertificationReport findReportValidatedByRepositoryIdAndMaxUpdateDate(String repositoryId);
	
	CertificationReport findReportInProgressByRepositoryIdAndMaxUpdateDate(String repositoryId);


}
