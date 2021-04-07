package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;

public interface CommentsRepository extends MongoRepository<RequirementComments, String> {
	
	List<RequirementComments> findByReportId(String reportId);
	RequirementComments findByReportIdAndItemCode(String reportId, Integer requirementCode);

}
