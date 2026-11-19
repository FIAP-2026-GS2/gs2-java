package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.response.IssueSummaryResponse;
import br.com.sampatech.portal.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final IssueService issueService;

    @GetMapping("/")
    public String home(Model model) {
        Page<IssueSummaryResponse> latest = issueService.search(null, null, null, null, null,
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "openingDate")));
        long total = latest.getTotalElements();
        long resolved = issueService.search(null, "RESOLVED", null, null, null, PageRequest.of(0, 1))
                .getTotalElements();

        model.addAttribute("latestIssues", latest.getContent());
        model.addAttribute("totalIssues", total);
        model.addAttribute("resolvedIssues", resolved);
        model.addAttribute("openIssues", total - resolved);
        return "index";
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal UserDetails me) {
        return me != null ? "redirect:/" : "login";
    }
}
