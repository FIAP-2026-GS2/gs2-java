package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.dto.request.MunicipalAgencyRequest;
import br.com.sampatech.portal.dto.response.MunicipalAgencyResponse;
import br.com.sampatech.portal.service.MunicipalAgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final MunicipalAgencyService agencyService;

    @GetMapping
    public ResponseEntity<Page<MunicipalAgencyResponse>> findAll(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(agencyService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MunicipalAgencyResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(agencyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MunicipalAgencyResponse> create(@RequestBody @Valid MunicipalAgencyRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agencyService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MunicipalAgencyResponse> update(@PathVariable Long id,
                                                          @RequestBody @Valid MunicipalAgencyRequest req) {
        return ResponseEntity.ok(agencyService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        agencyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}