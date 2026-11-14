package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.dto.request.UpdateProfileRequest;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails me) {
        return ResponseEntity.ok(userService.getProfile(me.getUsername()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UserDetails me,
                                                      @RequestBody @Valid UpdateProfileRequest req) {
        return ResponseEntity.ok(userService.updateProfile(me.getUsername(), req));
    }

    @DeleteMapping
    public ResponseEntity<Void> deactivate(@AuthenticationPrincipal UserDetails me) {
        userService.deactivateOwnAccount(me.getUsername());
        return ResponseEntity.noContent().build();
    }
}