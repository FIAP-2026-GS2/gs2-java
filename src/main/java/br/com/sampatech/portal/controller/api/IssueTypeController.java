package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.service.IssueTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issue-types")
@RequiredArgsConstructor
public class IssueTypeController {

    private final IssueTypeService issueTypeService;

    @GetMapping
    public ResponseEntity<List<IssueTypeResponse>> listActive() {
        return ResponseEntity.ok(issueTypeService.listActive());
    }
}