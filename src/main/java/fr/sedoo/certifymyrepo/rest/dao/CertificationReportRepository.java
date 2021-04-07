package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;

public interface CertificationReportRepository extends MongoRepository<CertificationReport, String> {
	
	List<CertificationReport> findByRepositoryId(String repositoryId);
	
	void deleteByRepositoryId(String repositoryId);
	
	CertificationReport findFirstByRepositoryId(String repositoryId, Sort sort);
	
	CertificationReport findFirstByRepositoryIdAndStatusIn(String repositoryId, String[] statusArray, Sort sort);
	

}
