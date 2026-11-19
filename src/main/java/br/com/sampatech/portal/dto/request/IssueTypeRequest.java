package br.com.sampatech.portal.dto.request;

import br.com.sampatech.portal.entity.IssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IssueTypeRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter até 100 caracteres")
        String name,

        @NotNull(message = "Categoria é obrigatória")
        IssueCategory category,

        @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
        String description
) {}
