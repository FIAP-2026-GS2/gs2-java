package br.com.sampatech.notification.messaging;

import java.time.LocalDateTime;

public record UserRegisteredEvent(Long userId, String name, String email, LocalDateTime registeredAt) {}