package br.com.sampatech.portal.service;

import br.com.sampatech.portal.dto.response.*;
import br.com.sampatech.portal.entity.IssueReport;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.repository.IssueHistoryRepository;
import br.com.sampatech.portal.repository.IssueReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueService {

    private final IssueReportRepository issueReportRepository;
    private final IssueHistoryRepository issueHistoryRepository;

    public Page<IssueSummaryResponse> search(Long typeId, String status, String neighborhood,
                                             LocalDate from, LocalDate to, Pageable pageable) {
        LocalDateTime start = from != null ? from.atStartOfDay() : null;
        LocalDateTime end = to != null ? to.atTime(23, 59, 59) : null;

        return issueReportRepository
                .search(typeId, blankToNull(status, true), blankToNull(neighborhood, false), start, end, pageable)
                .map(IssueSummaryResponse::from);
    }

    public IssueDetailResponse findById(Long id) {
        IssueReport issue = issueReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrência " + id + " não encontrada"));

        List<IssueHistoryResponse> history = issueHistoryRepository
                .findByIssueReportIdOrderByChangeDateAsc(id).stream()
                .map(IssueHistoryResponse::from)
                .toList();

        return new IssueDetailResponse(
                IssueSummaryResponse.from(issue),
                issue.getDescription(),
                issue.getLatitude(),
                issue.getLongitude(),
                history
        );
    }

    private String blankToNull(String value, boolean upper) {
        if (value == null || value.isBlank()) return null;
        return upper ? value.trim().toUpperCase() : value.trim();
    }
}