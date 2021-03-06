package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
import fr.sedoo.certifymyrepo.rest.dto.RequirementCommentsDto;

public interface CommentsDao {
	
	public List<RequirementCommentsDto> getCommentsByReportId(String reportId);
	public List<RequirementCommentsDto> getCommentsByUserId(String userId);
	public RequirementComments getCommentsByReportIdAndRequirementCode(String reportId, String requirementCode);
	public RequirementComments save(RequirementComments comments);
	public void deleteByReportId(String repository);

}
