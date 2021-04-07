package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.CertificationReport;
import fr.sedoo.certifymyrepo.rest.domain.ReportStatus;

@Component
public class CertificationReportDaoMongoImpl implements CertificationReportDao {

	@Autowired
	CertificationReportRepository certificationReportRepository;
	
	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public CertificationReport save(CertificationReport certificationReport) {
		return certificationReportRepository.save(certificationReport);
		
	}

	@Override
	public void delete(String id) {
		certificationReportRepository.deleteById(id);
	}
	
	@Override
	public void deleteByRepositoryId(String repositoryId) {
		certificationReportRepository.deleteByRepositoryId(repositoryId);
	}

	@Override
	public List<CertificationReport> findByRepositoryId(String repositoryId) {
		return certificationReportRepository.findByRepositoryId(repositoryId);

	}

	@Override
	public CertificationReport findById(String id) {
		Optional<CertificationReport> report = certificationReportRepository.findById(id);
		return report.get();
	}

	@Override
	public CertificationReport findByRepositoryIdAndMaxUpdateDate(String repositoryId) {
		return certificationReportRepository.findFirstByRepositoryId(repositoryId, Sort.by(Sort.Direction.DESC, "updateDate"));
	}

	@Override
	public CertificationReport findReportValidatedByRepositoryIdAndMaxUpdateDate(String repositoryId) {
		return certificationReportRepository.findFirstByRepositoryIdAndStatusIn(
				repositoryId, new String[] {ReportStatus.RELEASED.name()}, 
				Sort.by(Sort.Direction.DESC, "updateDate"));
	}

	@Override
	public CertificationReport findReportInProgressByRepositoryIdAndMaxUpdateDate(String repositoryId) {
		return certificationReportRepository.findFirstByRepositoryIdAndStatusIn(
				repositoryId, new String[] {ReportStatus.NEW.name(), ReportStatus.IN_PROGRESS.name()}, 
				Sort.by(Sort.Direction.DESC, "updateDate"));
	}
	
}
