package br.com.sampatech.portal.dto.response;

import br.com.sampatech.portal.entity.IssueCategory;
import br.com.sampatech.portal.entity.IssueType;

public record IssueTypeResponse(
        Long id, String name, IssueCategory category, String description, Boolean active
) {
    public static IssueTypeResponse from(IssueType t) {
        return new IssueTypeResponse(t.getId(), t.getName(), t.getCategory(),
                t.getDescription(), t.getActive());
    }
}