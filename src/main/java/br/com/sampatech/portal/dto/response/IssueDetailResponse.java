package br.com.sampatech.portal.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record IssueDetailResponse(
        IssueSummaryResponse summary,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        List<IssueHistoryResponse> history
) {}