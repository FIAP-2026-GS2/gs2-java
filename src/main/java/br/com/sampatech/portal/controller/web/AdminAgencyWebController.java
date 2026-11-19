package br.com.sampatech.portal.controller.web;

import br.com.sampatech.portal.config.SortRules;
import br.com.sampatech.portal.dto.form.AgencyForm;
import br.com.sampatech.portal.dto.request.MunicipalAgencyRequest;
import br.com.sampatech.portal.dto.response.MunicipalAgencyResponse;
import br.com.sampatech.portal.exception.BusinessException;
import br.com.sampatech.portal.service.MunicipalAgencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyWebController {

    private final MunicipalAgencyService agencyService;

    @GetMapping
    public String list(@PageableDefault(size = 10, sort = "name") Pageable pageable, Model model) {
        model.addAttribute("page", agencyService.findAll(SortRules.filter(pageable, SortRules.AGENCY, Sort.by("name"))));
        return "admin/agencies";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        return showForm(model, new AgencyForm(), null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") AgencyForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model, form, null);
        }
        try {
            agencyService.create(toRequest(form));
        } catch (BusinessException e) {
            result.rejectValue("name", "duplicate", e.getMessage());
            return showForm(model, form, null);
        }
        redirect.addFlashAttribute("success", "Órgão " + form.getName() + " criado.");
        return "redirect:/admin/agencies";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        MunicipalAgencyResponse agency = agencyService.findById(id);
        AgencyForm form = new AgencyForm();
        form.setName(agency.name());
        form.setDescription(agency.description());
        form.setCity(agency.city());
        form.setState(agency.state());
        form.setSubprefecture(agency.subprefecture());
        return showForm(model, form, id);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") AgencyForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return showForm(model, form, id);
        }
        try {
            agencyService.update(id, toRequest(form));
        } catch (BusinessException e) {
            result.rejectValue("name", "duplicate", e.getMessage());
            return showForm(model, form, id);
        }
        redirect.addFlashAttribute("success", "Órgão " + form.getName() + " atualizado.");
        return "redirect:/admin/agencies";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        agencyService.deactivate(id);
        redirect.addFlashAttribute("success", "Órgão desativado.");
        return "redirect:/admin/agencies";
    }

    private String showForm(Model model, AgencyForm form, Long id) {
        model.addAttribute("form", form);
        model.addAttribute("agencyId", id);
        return "admin/agency-form";
    }

    private MunicipalAgencyRequest toRequest(AgencyForm form) {
        return new MunicipalAgencyRequest(
                form.getName().trim(),
                blankToNull(form.getDescription()),
                form.getCity().trim(),
                form.getState().trim().toUpperCase(),
                blankToNull(form.getSubprefecture()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
