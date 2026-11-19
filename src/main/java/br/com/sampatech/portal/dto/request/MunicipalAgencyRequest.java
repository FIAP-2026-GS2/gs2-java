package br.com.sampatech.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MunicipalAgencyRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter até 120 caracteres")
        String name,

        @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
        String description,

        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 100, message = "Cidade deve ter até 100 caracteres")
        String city,

        @NotBlank(message = "UF é obrigatória")
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
        String state,

        @Size(max = 50, message = "Subprefeitura deve ter até 50 caracteres")
        String subprefecture
) {}
