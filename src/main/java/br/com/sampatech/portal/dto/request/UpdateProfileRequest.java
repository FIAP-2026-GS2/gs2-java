package br.com.sampatech.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter até 120 caracteres")
        String name,

        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
        String password
) {}
