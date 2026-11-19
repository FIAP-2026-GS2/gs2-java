package br.com.sampatech.portal.dto.form;

import br.com.sampatech.portal.entity.IssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueTypeForm {

    @NotBlank(message = "Informe o nome")
    @Size(max = 100, message = "Nome deve ter até 100 caracteres")
    private String name;

    @NotNull(message = "Escolha a categoria")
    private IssueCategory category;

    @Size(max = 500, message = "Descrição deve ter até 500 caracteres")
    private String description;
}
