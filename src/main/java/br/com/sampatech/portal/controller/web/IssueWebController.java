package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.response.IssueSummaryResponse;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/issues")
@RequiredArgsConstructor
public class IssueWebController {

    private static final Map<String, String> STATUS_LABELS = new LinkedHashMap<>();
    private static final Map<String, String> SEVERITY_LABELS = new LinkedHashMap<>();

    static {
        STATUS_LABELS.put("OPEN", "Aberta");
        STATUS_LABELS.put("ASSIGNED", "Encaminhada");
        STATUS_LABELS.put("IN_PROGRESS", "Em andamento");
        STATUS_LABELS.put("VALIDATION", "Em validação");
        STATUS_LABELS.put("RESOLVED", "Resolvida");

        SEVERITY_LABELS.put("LOW", "Baixa");
        SEVERITY_LABELS.put("MEDIUM", "Média");
        SEVERITY_LABELS.put("HIGH", "Alta");
        SEVERITY_LABELS.put("CRITICAL", "Crítica");
    }

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

        Page<IssueSummaryResponse> page = issueService.search(typeId, status, neighborhood, from, to, pageable);

        searchTrackingService.track(typeId, neighborhood, "SITE", me != null ? me.getUsername() : null);

        model.addAttribute("page", page);
        model.addAttribute("types", issueTypeService.listActive());
        model.addAttribute("statusLabels", STATUS_LABELS);
        model.addAttribute("severityLabels", SEVERITY_LABELS);
        model.addAttribute("typeId", typeId);
        model.addAttribute("status", status);
        model.addAttribute("neighborhood", neighborhood);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "issues/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("issue", issueService.findById(id));
        } catch (ResourceNotFoundException e) {
            return "redirect:/issues?notFound";
        }
        model.addAttribute("statusLabels", STATUS_LABELS);
        model.addAttribute("severityLabels", SEVERITY_LABELS);
        return "issues/detail";
    }
}