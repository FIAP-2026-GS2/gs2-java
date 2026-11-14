package br.com.sampatech.portal.dto.request;

import br.com.sampatech.portal.entity.IssueCategory;
import jakarta.validation.constraints.*;

public record IssueTypeRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull IssueCategory category,
        @Size(max = 500) String description
) {}