package br.com.sampatech.portal.dto.request;

import br.com.sampatech.portal.entity.UserType;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateRequest(
        @NotNull UserType userType,
        @NotNull Boolean active
) {}