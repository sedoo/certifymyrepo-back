package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;

public interface CommentsDao {
	
	public List<RequirementComments> getCommentsByReportId(String reportId);
	public RequirementComments getCommentsByReportIdAndRequirementCode(String reportId, Integer requirementCode);
	public RequirementComments save(RequirementComments comments);

}
