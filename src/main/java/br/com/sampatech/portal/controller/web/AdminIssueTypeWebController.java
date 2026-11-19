package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.dto.form.IssueTypeForm;
import br.com.sampatech.portal.dto.request.IssueTypeRequest;
import br.com.sampatech.portal.dto.response.IssueTypeResponse;
import br.com.sampatech.portal.entity.IssueCategory;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.service.IssueTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/issue-types")
@RequiredArgsConstructor
public class AdminIssueTypeWebController {

    private final IssueTypeService issueTypeService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "name") Pageable pageable, Model model) {
        model.addAttribute("page", issueTypeService.findAll(pageable));
        return "admin/issue-types";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        return showForm(model, new IssueTypeForm(), null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") IssueTypeForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model, form, null);
        }
        try {
            issueTypeService.create(toRequest(form));
        } catch (BusinessException e) {
            result.rejectValue("name", "duplicate", e.getMessage());
            return showForm(model, form, null);
        }
        redirect.addFlashAttribute("success", "Tipo " + form.getName() + " criado.");
        return "redirect:/admin/issue-types";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        IssueTypeResponse type = issueTypeService.findById(id);
        IssueTypeForm form = new IssueTypeForm();
        form.setName(type.name());
        form.setCategory(type.category());
        form.setDescription(type.description());
        return showForm(model, form, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") IssueTypeForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model, form, id);
        }
        try {
            issueTypeService.update(id, toRequest(form));
        } catch (BusinessException e) {
            result.rejectValue("name", "duplicate", e.getMessage());
            return showForm(model, form, id);
        }
        redirect.addFlashAttribute("success", "Tipo " + form.getName() + " atualizado.");
        return "redirect:/admin/issue-types";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        issueTypeService.deactivate(id);
        redirect.addFlashAttribute("success", "Tipo desativado.");
        return "redirect:/admin/issue-types";
    }

    private String showForm(Model model, IssueTypeForm form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("typeId", id);
        model.addAttribute("categories", IssueCategory.values());
        return "admin/issue-type-form";
    }

    private IssueTypeRequest toRequest(IssueTypeForm form) {
        String description = form.getDescription() == null || form.getDescription().isBlank()
                ? null : form.getDescription().trim();
        return new IssueTypeRequest(form.getName().trim(), form.getCategory(), description);
    }
}
