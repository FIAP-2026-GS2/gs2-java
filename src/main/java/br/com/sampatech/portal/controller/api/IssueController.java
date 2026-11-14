package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.dto.response.IssueDetailResponse;
import br.com.sampatech.portal.dto.response.IssueSummaryResponse;
import br.com.sampatech.portal.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @GetMapping
    public ResponseEntity<Page<IssueSummaryResponse>> search(
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String neighborhood,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "openingDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(issueService.search(typeId, status, neighborhood, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDetailResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.findById(id));
    }
}