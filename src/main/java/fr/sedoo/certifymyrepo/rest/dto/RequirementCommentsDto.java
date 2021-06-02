package fr.sedoo.certifymyrepo.rest.dto;

import java.util.List;

import fr.sedoo.certifymyrepo.rest.domain.RequirementComments;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementCommentsDto {
	
	public RequirementCommentsDto(RequirementComments requirementComments, List<CommentDto> comments) {
		this.setId(requirementComments.getId());
		this.setItemCode(requirementComments.getItemCode());
		this.setReportId(requirementComments.getReportId());
		this.setComments(comments);
	}
	
	private String id;
	private String reportId;
	private String itemCode;
	private List<CommentDto> comments;

}
