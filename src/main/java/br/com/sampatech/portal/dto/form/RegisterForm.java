package br.com.sampatech.portal.dto.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {

    @NotBlank(message = "Informe seu nome")
    @Size(max = 120, message = "Nome deve ter até 120 caracteres")
    private String name;

    @NotBlank(message = "Informe seu e-mail")
    @Email(message = "E-mail inválido")
    private String email;
    @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
    private String password;
}