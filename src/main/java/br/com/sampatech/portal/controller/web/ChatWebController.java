package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.form.ChatMessage;
import br.com.sampatech.portal.dto.request.ChatRequest;
import br.com.sampatech.portal.dto.response.ChatAnswerResponse;
import br.com.sampatech.portal.service.ChatService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatWebController {

    private static final String CONVERSATION = "conversation";

    private final ChatService chatService;

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        model.addAttribute("conversation", getConversation(session));
        return "chat";
    }

    @PostMapping("/chat")
    public String ask(@RequestParam String message,
                      @AuthenticationPrincipal UserDetails me,
                      HttpSession session) {
        if (message == null || message.isBlank()) {
            return "redirect:/chat";
        }
        String question = message.length() > 1000 ? message.substring(0, 1000) : message.trim();

        String answer = chatService.ask(question, email(me), session.getId()).answer();
        getConversation(session).add(new ChatMessage(question, answer));

        return "redirect:/chat";
    }

    @PostMapping(value = "/chat", params = "clear")
    public String clear(@AuthenticationPrincipal UserDetails me, HttpSession session) {
        session.removeAttribute(CONVERSATION);
        chatService.clearMemory(email(me), session.getId());
        return "redirect:/chat";
    }

    @PostMapping("/chat/ask")
    @ResponseBody
    public ChatAnswerResponse askWidget(@RequestBody @Valid ChatRequest request,
                                        @AuthenticationPrincipal UserDetails me,
                                        HttpSession session) {
        String question = request.message().trim();

        ChatAnswerResponse response = chatService.ask(question, email(me), session.getId());
        getConversation(session).add(new ChatMessage(question, response.answer()));

        return response;
    }

    @GetMapping("/chat/history")
    @ResponseBody
    public List<ChatMessage> history(HttpSession session) {
        return getConversation(session);
    }

    @DeleteMapping("/chat/history")
    @ResponseBody
    public ResponseEntity<Void> clearHistory(@AuthenticationPrincipal UserDetails me, HttpSession session) {
        session.removeAttribute(CONVERSATION);
        chatService.clearMemory(email(me), session.getId());
        return ResponseEntity.noContent().build();
    }

    private String email(UserDetails me) {
        return me != null ? me.getUsername() : null;
    }

    @SuppressWarnings("unchecked")
    private List<ChatMessage> getConversation(HttpSession session) {
        List<ChatMessage> conversation = (List<ChatMessage>) session.getAttribute(CONVERSATION);
        if (conversation == null) {
            conversation = new ArrayList<>();
            session.setAttribute(CONVERSATION, conversation);
        }
        return conversation;
    }
}