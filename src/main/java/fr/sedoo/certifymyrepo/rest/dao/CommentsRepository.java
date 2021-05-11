package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;

public interface CommentsRepository extends MongoRepository<RequirementComments, String> {
	
	List<RequirementComments> findByReportId(String reportId);
	
	@Query(value = "{ 'comments' :  {$elemMatch: { 'userId': ?0 }} }")
	List<RequirementComments> findByUserId(String userId);
	
	RequirementComments findByReportIdAndItemCode(String reportId, String requirementCode);
	
	List<RequirementComments> deleteByReportId (String reportId);

}
