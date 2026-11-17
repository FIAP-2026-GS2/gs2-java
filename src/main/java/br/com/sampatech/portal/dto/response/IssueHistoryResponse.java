package br.com.sampatech.portal.dto.response;

import br.com.sampatech.portal.entity.IssueHistory;
import java.time.LocalDateTime;

public record IssueHistoryResponse(
        String previousStatus, String newStatus, String notes, LocalDateTime changeDate
) {
    public static IssueHistoryResponse from(IssueHistory h) {
        return new IssueHistoryResponse(h.getPreviousStatus(), h.getNewStatus(),
                h.getNotes(), h.getChangeDate());
    }
}