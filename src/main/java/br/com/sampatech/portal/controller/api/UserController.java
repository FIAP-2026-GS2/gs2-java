package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.config.SortRules;
import br.com.sampatech.portal.dto.request.UpdateProfileRequest;
import br.com.sampatech.portal.dto.response.ChatHistoryResponse;
import br.com.sampatech.portal.dto.response.UserResponse;
import br.com.sampatech.portal.service.ChatHistoryService;
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
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ChatHistoryService chatHistoryService;

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

    @GetMapping("/chat-history")
    public ResponseEntity<Page<ChatHistoryResponse>> chatHistory(
            @AuthenticationPrincipal UserDetails me,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(chatHistoryService.getHistory(me.getUsername(), SortRules.validate(pageable, SortRules.CHAT)));
    }
}