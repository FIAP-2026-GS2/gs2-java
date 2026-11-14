package br.com.sampatech.portal.dto.response;

import br.com.sampatech.portal.entity.*;
import java.time.LocalDateTime;

public record IssueSummaryResponse(
        Long id, String title, String issueType, IssueCategory category,
        String status, Severity severity, IssueSource source,
        String neighborhood, String street,
        LocalDateTime openingDate, LocalDateTime resolutionDate, boolean resolved
) {
    public static IssueSummaryResponse from(IssueReport i) {
        MonitoringPoint mp = i.getMonitoringPoint();
        return new IssueSummaryResponse(
                i.getId(), i.getTitle(),
                i.getIssueType().getName(), i.getIssueType().getCategory(),
                i.getStatus(), i.getSeverity(), i.getSource(),
                mp != null ? mp.getNeighborhood() : null,
                mp != null ? mp.getStreet() : null,
                i.getOpeningDate(), i.getResolutionDate(),
                i.getResolutionDate() != null
        );
    }
}