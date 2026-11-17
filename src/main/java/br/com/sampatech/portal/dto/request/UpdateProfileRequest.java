package br.com.sampatech.portal.dto.request;

import jakarta.validation.constraints.*;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres") String password
) {}