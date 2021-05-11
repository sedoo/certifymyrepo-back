package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;

public interface CommentsDao {
	
	public List<RequirementComments> getCommentsByReportId(String reportId);
	public List<RequirementComments> getCommentsByUserId(String userId);
	public RequirementComments getCommentsByReportIdAndRequirementCode(String reportId, String requirementCode);
	public RequirementComments save(RequirementComments comments);
	public void deleteByReportId(String repository);

}
