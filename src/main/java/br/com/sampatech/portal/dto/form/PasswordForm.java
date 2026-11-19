package br.com.sampatech.portal.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordForm {
    @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
    private String password;

    @NotBlank(message = "Confirme a nova senha")
    private String confirmPassword;
}
