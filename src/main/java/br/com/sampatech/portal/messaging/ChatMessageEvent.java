package br.com.sampatech.portal.messaging;

import java.time.LocalDateTime;

public record ChatMessageEvent(Long userId, String question, String answer, LocalDateTime createdAt) {}