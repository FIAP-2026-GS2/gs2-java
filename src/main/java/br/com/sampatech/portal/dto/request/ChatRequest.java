package br.com.sampatech.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "A pergunta não pode ser vazia")
        @Size(max = 1000, message = "A pergunta deve ter até 1000 caracteres")
        String message
) {}