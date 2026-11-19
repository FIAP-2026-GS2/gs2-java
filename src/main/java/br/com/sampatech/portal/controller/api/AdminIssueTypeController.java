package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.config.SortRules;
import br.com.sampatech.portal.dto.request.IssueTypeRequest;
import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.service.IssueTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/issue-types")
@RequiredArgsConstructor
public class AdminIssueTypeController {

    private final IssueTypeService issueTypeService;

    @GetMapping
    public ResponseEntity<Page<IssueTypeResponse>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(issueTypeService.findAll(SortRules.validate(pageable, SortRules.ISSUE_TYPE)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueTypeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(issueTypeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<IssueTypeResponse> create(@RequestBody @Valid IssueTypeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueTypeService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueTypeResponse> update(@PathVariable Long id,
                                                    @RequestBody @Valid IssueTypeRequest req) {
        return ResponseEntity.ok(issueTypeService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        issueTypeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}