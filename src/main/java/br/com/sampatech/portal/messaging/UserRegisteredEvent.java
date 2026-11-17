package br.com.sampatech.portal.messaging;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        Long userId,
        String name,
        String email,
        LocalDateTime registeredAt
) {}