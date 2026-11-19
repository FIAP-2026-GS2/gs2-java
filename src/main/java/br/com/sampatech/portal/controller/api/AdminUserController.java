package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.config.SortRules;
import br.com.sampatech.portal.dto.request.AdminUserUpdateRequest;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(SortRules.validate(pageable, SortRules.USER)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid AdminUserUpdateRequest req,
                                               @AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok(userService.adminUpdate(id, req, me.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, @AuthenticationPrincipal UserDetails me) {
        userService.deactivate(id, me.getUsername());
        return ResponseEntity.noContent().build();
    }
}