package br.com.sampatech.portal.dto.form;

import br.com.sampatech.portal.entity.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserAdminForm {

    @NotNull(message = "Escolha o tipo de usuário")
    private UserType userType;

    private boolean active;
}
