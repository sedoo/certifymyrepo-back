package fr.sedoo.certifymyrepo.rest.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;

@Component
public class CommentsDaoMongoImpl implements CommentsDao {
	
	@Autowired
	private CommentsRepository repository;

	@Override
	public List<RequirementComments> getCommentsByReportId(String reportId) {
		return repository.findByReportId(reportId);
	}

	@Override
	public RequirementComments save(RequirementComments comments) {
		return repository.save(comments);
	}

	@Override
	public RequirementComments getCommentsByReportIdAndRequirementCode(String reportId, Integer requirementCode) {
		return repository.findByReportIdAndItemCode(reportId, requirementCode);
	}

}
