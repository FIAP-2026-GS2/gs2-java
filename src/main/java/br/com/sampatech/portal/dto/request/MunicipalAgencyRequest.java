package br.com.sampatech.portal.dto.request;

import jakarta.validation.constraints.*;

public record MunicipalAgencyRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas") String state,
        @Size(max = 50) String subprefecture
) {}