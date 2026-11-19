package br.com.sampatech.portal.dto.request;

import br.com.sampatech.portal.entity.UserType;
import jakarta.validation.constraints.NotNull;

public record AdminUserUpdateRequest(
        @NotNull(message = "Tipo de usuário é obrigatório (CITIZEN ou ADMIN)")
        UserType userType,

        @NotNull(message = "Informe se a conta está ativa (true ou false)")
        Boolean active
) {}
