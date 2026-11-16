package br.com.sampatech.portal.dto.response;

import br.com.sampatech.portal.entity.ChatHistory;
import java.time.LocalDateTime;

public record ChatHistoryResponse(Long id, String question, String answer, LocalDateTime createdAt) {
    public static ChatHistoryResponse from(ChatHistory c) {
        return new ChatHistoryResponse(c.getId(), c.getQuestion(), c.getAnswer(), c.getCreatedAt());
    }
}