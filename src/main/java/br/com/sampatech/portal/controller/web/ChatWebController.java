package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.form.ChatMessage;
import br.com.sampatech.portal.service.ChatService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        String email = me != null ? me.getUsername() : null;

        String answer = chatService.ask(question, email).answer();
        getConversation(session).add(new ChatMessage(question, answer));

        return "redirect:/chat";
    }

    @PostMapping(value = "/chat", params = "clear")
    public String clear(HttpSession session) {
        session.removeAttribute(CONVERSATION);
        return "redirect:/chat";
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