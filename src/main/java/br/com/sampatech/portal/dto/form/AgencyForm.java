package br.com.sampatech.portal.dto.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AgencyForm {

    @NotBlank(message = "Informe o nome")
    @Size(max = 120, message = "Nome deve ter até 120 caracteres")
    private String name;

    @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
    private String description;

    @NotBlank(message = "Informe a cidade")
    @Size(max = 100, message = "Cidade deve ter até 100 caracteres")
    private String city = "São Paulo";

    @NotBlank(message = "Informe a UF")
    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
    private String state = "SP";

    @Size(max = 50, message = "Subprefeitura deve ter até 50 caracteres")
    private String subprefecture;
}
