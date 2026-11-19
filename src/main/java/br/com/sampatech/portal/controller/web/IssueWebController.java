package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.config.SortRules;
import br.com.sampatech.portal.dto.response.IssueDetailResponse;
import br.com.sampatech.portal.dto.response.IssueSummaryResponse;
import br.com.sampatech.portal.exception.BadRequestException;
import br.com.sampatech.portal.exception.ResourceNotFoundException;
import br.com.sampatech.portal.service.IssueService;
import br.com.sampatech.portal.service.IssueTypeService;
import br.com.sampatech.portal.service.SearchTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/issues")
@RequiredArgsConstructor
public class IssueWebController {

    private final IssueService issueService;
    private final IssueTypeService issueTypeService;
    private final SearchTrackingService searchTrackingService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails me,
                       @RequestParam(required = false) Long typeId,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String neighborhood,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @PageableDefault(size = 10, sort = "openingDate", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {

        Pageable safePageable = SortRules.filter(pageable, SortRules.ISSUE, Sort.by(Sort.Direction.DESC, "openingDate"));
        Page<IssueSummaryResponse> page;
        try {
            page = issueService.search(typeId, status, neighborhood, from, to, safePageable);
            searchTrackingService.track(typeId, neighborhood, "SITE", me != null ? me.getUsername() : null);
        } catch (BadRequestException e) {
            page = Page.empty(safePageable);
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("page", page);
        model.addAttribute("types", issueTypeService.listActive());
        model.addAttribute("typeId", typeId);
        model.addAttribute("status", status);
        model.addAttribute("neighborhood", neighborhood);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "issues/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        IssueDetailResponse issue;
        try {
            issue = issueService.findById(id);
        } catch (ResourceNotFoundException e) {
            return "redirect:/issues?notFound";
        }

        IssueSummaryResponse summary = issue.summary();
        model.addAttribute("issue", issue);
        model.addAttribute("statusSteps", WebModelAdvice.STATUS_FLOW.stream()
                .map(WebModelAdvice::statusLabel)
                .toList());
        model.addAttribute("currentStep", WebModelAdvice.STATUS_FLOW.indexOf(summary.status()));
        model.addAttribute("durationText", durationText(summary));
        return "issues/detail";
    }

    private String durationText(IssueSummaryResponse summary) {
        if (summary.openingDate() == null) {
            return "";
        }
        LocalDateTime end = summary.resolutionDate() != null ? summary.resolutionDate() : LocalDateTime.now();
        long days = Math.max(0, ChronoUnit.DAYS.between(summary.openingDate(), end));
        String unit = days == 1 ? " dia" : " dias";

        if (summary.resolutionDate() != null) {
            return days == 0 ? "Resolvida no mesmo dia" : "Resolvida em " + days + unit;
        }
        return days == 0 ? "Aberta hoje" : "Aberta há " + days + unit;
    }
}
