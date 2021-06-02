package fr.sedoo.certifymyrepo.rest.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.sedoo.certifymyrepo.rest.domain.Comment;
import fr.sedoo.certifymyrepo.rest.domain.Profile;
import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
import fr.sedoo.certifymyrepo.rest.dto.CommentDto;
import fr.sedoo.certifymyrepo.rest.dto.RequirementCommentsDto;

@Component
public class CommentsDaoMongoImpl implements CommentsDao {
	
	@Autowired
	private CommentsRepository repository;
	
	@Autowired
	private ProfileDao userProfile;

	@Override
	public List<RequirementCommentsDto> getCommentsByReportId(String reportId) {
		return domainToDto(repository.findByReportId(reportId));
	}

	@Override
	public RequirementComments save(RequirementComments comments) {
		return repository.save(comments);
	}

	@Override
	public RequirementComments getCommentsByReportIdAndRequirementCode(String reportId, String requirementCode) {
		return repository.findByReportIdAndItemCode(reportId, requirementCode);
	}

	@Override
	public List<RequirementCommentsDto> getCommentsByUserId(String userId) {
		return domainToDto(repository.findByUserId(userId));
	}

	@Override
	public void deleteByReportId(String reportId) {
		repository.deleteByReportId(reportId);
	}
	
	private List<RequirementCommentsDto> domainToDto(List<RequirementComments> requirementComments) {
		List<RequirementCommentsDto> result = null; 
		if(requirementComments != null) {
			result = new ArrayList<RequirementCommentsDto>();
			for(RequirementComments requirementComment : requirementComments) {
				List<CommentDto> commentsDto = new ArrayList<CommentDto>();
				for(Comment comment : requirementComment.getComments()) {
					String name = null;
					Optional<Profile> profile = userProfile.findById(comment.getUserId());
					if(profile.isPresent()) {
						name = profile.get().getName();
					}
					commentsDto.add(new CommentDto(comment, name));
				}
				result.add(new RequirementCommentsDto(requirementComment, commentsDto));
			}
		}
		return result;
	}

}
