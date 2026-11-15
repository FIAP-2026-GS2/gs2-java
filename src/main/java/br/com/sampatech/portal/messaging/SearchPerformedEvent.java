package br.com.sampatech.portal.messaging;

import java.time.LocalDateTime;

public record SearchPerformedEvent(
        Long userId,
        Long issueTypeId,
        String neighborhood,
        String origin,
        LocalDateTime searchedAt
) {}