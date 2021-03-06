package fr.sedoo.certifymyrepo.rest.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;

public interface CertificationReportRepository extends MongoRepository<CertificationReport, String> {
	
	List<CertificationReport> findByRepositoryId(String repositoryId, Sort sort);
	
	List<CertificationReport> deleteByRepositoryId(String repositoryId);
	
	CertificationReport findFirstByRepositoryId(String repositoryId, Sort sort);
	
	CertificationReport findFirstByRepositoryIdAndStatusIn(String repositoryId, String[] statusArray, Sort sort);
	
	@Query(value = "{$and:[{'updateDate' :  {$lte: ?0} }, {'status' : { $ne: 'RELEASED' } }, {$or:[{'lastNotificationDate' :  { $exists : false } }, {'lastNotificationDate' :  null }, {'lastNotificationDate' :  {$lte: ?0} }]} ]}")
    List<CertificationReport> findInProgressByUpdateDateLowerThan(Date date);
	
	@Query(value = "{'status' :  'IN_PROGRESS'}", count = true)
    long countInprogress();
	
	@Query(value = "{'status' :  'RELEASED'}", count = true)
    long countValidated();
	

}
