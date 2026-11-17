package br.com.sampatech.portal.controller.api;

import br.com.sampatech.portal.dto.request.ChatRequest;
import br.com.sampatech.portal.dto.response.ChatAnswerResponse;
import br.com.sampatech.portal.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatAnswerResponse> ask(@AuthenticationPrincipal UserDetails me,
                                                  @RequestBody @Valid ChatRequest req) {
        String email = (me != null) ? me.getUsername() : null;
        return ResponseEntity.ok(chatService.ask(req.message(), email));
    }
}